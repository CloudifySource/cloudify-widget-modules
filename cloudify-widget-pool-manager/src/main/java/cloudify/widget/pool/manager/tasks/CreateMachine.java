package cloudify.widget.pool.manager.tasks;

import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 5:32 PM
 */
public class CreateMachine extends AbstractPoolTask<TaskConfig, Collection<NodeModel>> {

    private static Logger logger = LoggerFactory.getLogger(CreateMachine.class);

    @Autowired
    private NodesDao nodesDao;


    @Override
    public TaskName getTaskName() {
        return TaskName.CREATE_MACHINE;
    }

    @Override
    public Collection<NodeModel> call() throws Exception {
        logger.info("creating machine with pool settings [{}]", poolSettings);

        ProviderSettings providerSettings = poolSettings.getProvider();


        CloudServerApi cloudServerApi = CloudServerApiFactory.create(providerSettings.getName());
        if (cloudServerApi == null) {
            String message = String.format("failed to obtain cloud server API using provider [%s]", providerSettings.getName());
            logger.error(message);
            throw new RuntimeException(message);
        }


        logger.info("connecting to provider [{}]", providerSettings.getName());
        cloudServerApi.connect(providerSettings.getConnectDetails());

        Collection<NodeModel> nodeModelsCreated = new ArrayList<NodeModel>();

        Collection<? extends CloudServerCreated> cloudServerCreateds = cloudServerApi.create(providerSettings.getMachineOptions());
        for (CloudServerCreated created : cloudServerCreateds) {
            NodeModel nodeModel = new NodeModel()
                    .setMachineId(created.getId())
                    .setPoolId(poolSettings.getUuid())
                    .setNodeStatus(NodeStatus.CREATED)
                    .setMachineSshDetails(created.getSshDetails());
            logger.info("machine created, adding node to database. node model is [{}]", nodeModel);
            nodesDao.create(nodeModel);
            nodeModelsCreated.add(nodeModel);
        }

        // TODO ponder: do we really need to pass this back?
        return nodeModelsCreated;
    }

}
