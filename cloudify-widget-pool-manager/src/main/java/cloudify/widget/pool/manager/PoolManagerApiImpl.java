package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.actions.PingAction;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.node_management.DecisionsDao;
import cloudify.widget.pool.manager.node_management.NodeManagementMode;
import cloudify.widget.pool.manager.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Collection;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 7:50 PM
 */
public class PoolManagerApiImpl implements PoolManagerApi, ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(PoolManagerApiImpl.class);

    private NodesDao nodesDao;

    private ErrorsDao errorsDao;

    private ITasksDao tasksDao;

    private NodeMappingsDao nodeMappingsDao;

    private DecisionsDao decisionsDao;

    private StatusManager statusManager;

    private TaskExecutor taskExecutor;

    private String bootstrapSuccessText;

    private ApplicationContext applicationContext;

    private PingAction pingAction;


    @Override
    public PoolStatus getStatus(PoolSettings poolSettings) {
        if (poolSettings == null) return null;
        return statusManager.getPoolStatus(poolSettings);
    }

    @Override
    public Collection<PoolStatus> listStatuses() {
        return statusManager.listPoolStatuses();
    }

    @Override
    public List<NodeModel> listNodes(PoolSettings poolSettings) {
        if (poolSettings == null) return null;
        return nodesDao.readAllOfPool(poolSettings.getUuid());
    }

    @Override
    public NodeModel getNode(long nodeId) {
        return nodesDao.read(nodeId);
    }

    @Override
    public void createNode(PoolSettings poolSettings, TaskCallback<Collection<NodeModel>> taskCallback) {
        if (poolSettings == null) return;
        taskExecutor.execute(getCreateMachineTask(), null, poolSettings, taskCallback);
    }

    @Override
    public void deleteNode(PoolSettings poolSettings, long nodeId, TaskCallback<Void> taskCallback) {

        final NodeModel node = _getNodeModel(nodeId);
//        logger.info("deleting node [{}]", node.machineId);
        if (node == null) return;
        if (poolSettings == null) return;
        taskExecutor.execute(getDeleteMachineTask(), new DeleteMachineConfig() {
            @Override
            public NodeModel getNodeModel() {
                return node;
            }
        }, poolSettings, taskCallback);
    }

    @Override
    public void deleteCloudNode(PoolSettings poolSettings, String machineId, TaskCallback<Void> taskCallback) {
        final NodeModel node = new NodeModel();
        node.setMachineId(machineId);

        if (poolSettings == null) return;
        taskExecutor.execute(getDeleteMachineTask(), new DeleteMachineConfig() {
            @Override
            public NodeModel getNodeModel() {
                return node;
            }
        }, poolSettings, taskCallback);
    }

    @Override
    public void bootstrapNode(PoolSettings poolSettings, long nodeId, TaskCallback<NodeModel> taskCallback) {
        final NodeModel node = _getNodeModel(nodeId);
        taskExecutor.execute(getBootstrapMachineTask(), new BootstrapMachineConfig() {
            @Override
            public String getBootstrapSuccessText() {
                return bootstrapSuccessText;
            }

            @Override
            public NodeModel getNodeModel() {
                return node;
            }
        }, poolSettings, taskCallback);
    }

    @Override
    public PingResult pingNode(PoolSettings poolSettings, long nodeId) {
        final NodeModel node = _getNodeModel(nodeId);
        PingResult pingResult = new PingResult();

        if (poolSettings.getNodeManagement().getPingSettings() == null) {
            pingResult.setPingStatus(PingStatus.PING_SETTINGS_UNDEFINED);

        } else {
            pingResult.setPingResponses(pingAction.pingAll(node.machineSshDetails.getPublicIp(), poolSettings.getNodeManagement().getPingSettings()));
            boolean ping = pingResult.isAggregatedPingResponse();
            pingResult.setPingStatus(ping ? PingStatus.PING_SUCCESS : PingStatus.PING_FAIL);

        }

        nodesDao.updatePing(node.id, pingResult);

        return pingResult;
    }

    @Override
    public void expireNode(PoolSettings poolSettings, long nodeId) {
        nodesDao.updateExpires(nodeId, 0);
    }

    @Override
    public List<ErrorModel> listErrors(PoolSettings poolSettings) {
        if (poolSettings == null) return null;
        return errorsDao.readAllOfPool(poolSettings.getUuid());
    }

    @Override
    public void deleteErrors(PoolSettings poolSettings) {
        if (poolSettings == null) return;
        errorsDao.deleteAllOfPool(poolSettings.getUuid());
    }

    @Override
    public ErrorModel getError(long errorId) {
        return errorsDao.read(errorId);
    }

    @Override
    public void deleteError(long errorId) {
        errorsDao.delete(errorId);
    }

    @Override
    public List<TaskModel> listRunningTasks(PoolSettings poolSettings) {
        return tasksDao.readAllOfPool(poolSettings.getUuid());
    }

    @Override
    public void removeRunningTask(long taskId) {
        tasksDao.delete(taskId);
    }

    private NodeModel _getNodeModel(long nodeId) {
        final NodeModel node = nodesDao.read(nodeId);
        if (node == null) {
            logger.error("node with id [{}] not found", nodeId);
        }
        return node;
    }

    @Override
    public NodeModel occupy(PoolSettings poolSettings, long expires) {
        return nodesDao.occupyNode(poolSettings, expires);
    }

    @Override
    public List<NodeMappings> listCloudNodes(PoolSettings poolSettings) {
        return nodeMappingsDao.readAll(poolSettings);
    }

    @Override
    public List<DecisionModel> listDecisions(PoolSettings poolSettings) {
        return decisionsDao.readAllOfPool(poolSettings.getUuid());
    }

    @Override
    public void abortDecision(PoolSettings poolSettings, long decisionId) {
        decisionsDao.deleteIfNotApprovedAndNotExecuted(decisionId);
    }

    @Override
    public void updateDecisionApproval(PoolSettings poolSettings, long decisionId, boolean approved) {
        // check if mode allows to change approval
        NodeManagementMode nodeManagementMode = poolSettings.getNodeManagement().getMode();
        if (nodeManagementMode != NodeManagementMode.MANUAL_APPROVAL && nodeManagementMode != NodeManagementMode.MANUAL) {
            throw new RuntimeException(
                    String.format("update of 'approved' state of decisions is not allowed in mode [%s]", nodeManagementMode));
        }
        DecisionModel decisionModel = decisionsDao.read(decisionId);
        // nothing to change
        if (decisionModel.approved == approved) {
            return;
        }
        decisionsDao.update(decisionModel.setApproved(approved));
    }

    @Override
    public void cleanPool(PoolSettings poolSettings) {
        if (poolSettings == null) return;

        List<NodeModel> bootstrappedNodes = nodesDao.readAllOfPoolWithStatus(poolSettings.getUuid(), NodeStatus.BOOTSTRAPPED);
        for (final NodeModel node : bootstrappedNodes) {
            logger.info("deleting bootstrapped node [{}]", node);
            taskExecutor.execute(getDeleteMachineTask(), new DeleteMachineConfig() {
                @Override
                public NodeModel getNodeModel() {
                    return node;
                }
            }, poolSettings);
        }
    }



    private Task getBootstrapMachineTask() {
        return applicationContext.getBean(BootstrapMachine.class);
    }

    private Task getCreateMachineTask() {
        return applicationContext.getBean(CreateMachine.class);
    }

    private Task getDeleteMachineTask() {
        return applicationContext.getBean(DeleteMachine.class);
    }


    public void setErrorsDao(ErrorsDao errorsDao) {
        this.errorsDao = errorsDao;
    }

    public void setTasksDao(ITasksDao tasksDao) {
        this.tasksDao = tasksDao;
    }

    public void setNodesDao(NodesDao nodesDao) {
        this.nodesDao = nodesDao;
    }

    public void setNodeMappingsDao(NodeMappingsDao nodeMappingsDao) {
        this.nodeMappingsDao = nodeMappingsDao;
    }

    public void setDecisionsDao(DecisionsDao decisionsDao) {
        this.decisionsDao = decisionsDao;
    }

    public void setStatusManager(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setBootstrapSuccessText(String bootstrapSuccessText) {
        this.bootstrapSuccessText = bootstrapSuccessText;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void setPingAction(PingAction pingAction) {
        this.pingAction = pingAction;
    }
}
