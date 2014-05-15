package cloudify.widget.pool.manager.tasks;

import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.StatusManager;
import cloudify.widget.pool.manager.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 5:32 PM
 */
public class DeleteMachine extends AbstractPoolTask<DeleteMachineConfig, Void> {

    private static Logger logger = LoggerFactory.getLogger(DeleteMachine.class);

    @Autowired
    private NodesDao nodesDao;

    @Autowired
    private StatusManager statusManager;

    @Autowired
    private ErrorsDao errorsDao;

    @Override
    public Void call() throws Exception {
        logger.info("deleting machine [{}] with pool settings [{}]", taskConfig.getNodeModel().machineId, poolSettings);

        ProviderSettings providerSettings = poolSettings.getProvider();

        CloudServerApi cloudServerApi = CloudServerApiFactory.create(providerSettings.getName());
        if (cloudServerApi == null) {
            String message = String.format("failed to obtain an API object using provider [%s]", providerSettings.getName());
            logger.error(message);
            throw new RuntimeException(message);
        }

        PoolStatus status = statusManager.getPoolStatus(poolSettings);
        if (status.getCurrentSize() <= poolSettings.getMinNodes()) {
            String message = "pool has reached its minimum capacity as defined in the pool settings";
            logger.error(message);
            errorsDao.create(new ErrorModel()
                            .setTaskName(getTaskName())
                            .setPoolId(poolSettings.getUuid())
                            .setMessage(message)
            );
            throw new RuntimeException(message);
        }

        cloudServerApi.connect(providerSettings.getConnectDetails());
        cloudServerApi.delete(taskConfig.getNodeModel().machineId);

        logger.info("machine deleted, removing node model in the database [{}]", taskConfig.getNodeModel().machineId);
        nodesDao.delete(taskConfig.getNodeModel().id);

        return null;
    }


    @Override
    public TaskName getTaskName() {
        return TaskName.DELETE_MACHINE;
    }

    @Override
    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    @Override
    public void setTaskConfig(DeleteMachineConfig taskConfig) {
        this.taskConfig = taskConfig;
    }
}
