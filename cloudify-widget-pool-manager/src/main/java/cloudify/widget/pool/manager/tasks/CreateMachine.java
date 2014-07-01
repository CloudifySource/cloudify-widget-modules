package cloudify.widget.pool.manager.tasks;

import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.MachineTimeout;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.ErrorModel;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 5:32 PM
 */
public class CreateMachine extends AbstractPoolTask<TaskConfig, Collection<NodeModel>> {

    private static Logger logger = LoggerFactory.getLogger(CreateMachine.class);


    @Autowired
    private MachineTimeout defaultMachineTimeout;

    @Autowired
    private NodesDao nodesDao;




    @Override
    public TaskName getTaskName() {
        return TaskName.CREATE_MACHINE;
    }

    @Override
    public Collection<NodeModel> call() throws Exception {
        try {
            logger.info("creating machine with pool settings [{}]", poolSettings);

            ProviderSettings providerSettings = poolSettings.getProvider();

            CloudServerApi cloudServerApi = CloudServerApiFactory.create(providerSettings.getName());

            logger.info("connecting to provider [{}]", providerSettings.getName());
            cloudServerApi.connect(providerSettings.getConnectDetails());

            Collection<NodeModel> nodeModelCreatedList = new ArrayList<NodeModel>();

            Collection<? extends CloudServerCreated> cloudServerCreatedList = cloudServerApi.create(providerSettings.getMachineOptions());
            for (CloudServerCreated created : cloudServerCreatedList) {
                NodeModel nodeModel = new NodeModel()
                        .setMachineId(created.getId())
                        .setPoolId(poolSettings.getUuid())
                        .setNodeStatus(NodeStatus.CREATED)
                        .setMachineSshDetails(created.getSshDetails())
                        .setExpires(System.currentTimeMillis() + (defaultMachineTimeout.inMillis()));
                logger.info("machine created, adding node to database. node model is [{}]", nodeModel);
                nodesDao.create(nodeModel);
                nodeModelCreatedList.add(nodeModel);
            }

            return nodeModelCreatedList;
        }catch(Exception e){
            errorsDao.create(new ErrorModel()
                    .setPoolId(poolSettings.getUuid())
                    .setSource(getTaskName().name())
                    .setMessage(e.getMessage()));
        }
        return new LinkedList<NodeModel>();
    }

}
