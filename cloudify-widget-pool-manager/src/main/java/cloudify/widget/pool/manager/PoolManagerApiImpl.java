package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.tasks.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 7:50 PM
 */
public class PoolManagerApiImpl implements PoolManagerApi {

    private static Logger logger = LoggerFactory.getLogger(PoolManagerApiImpl.class);

    private NodesDao nodesDao;

    private ErrorsDao errorsDao;

    private ITasksDao tasksDao;

    private NodeMappingsDao nodeMappingsDao;

    private StatusManager statusManager;

    private TaskExecutor taskExecutor;

    private String bootstrapScriptResourcePath;

    private Task createMachineTask;

    private Task deleteMachineTask;

    private Task bootstrapMachineTask;

//    private



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
        taskExecutor.execute(createMachineTask, null, poolSettings, taskCallback);
    }

    @Override
    public void deleteNode(PoolSettings poolSettings, long nodeId,  TaskCallback<Void> taskCallback) {
        final NodeModel node = _getNodeModel(nodeId);
        if (node == null) return;
        if (poolSettings == null) return;
        taskExecutor.execute(deleteMachineTask, new DeleteMachineConfig() {
            @Override
            public NodeModel getNodeModel() {
                return node;
            }
        }, poolSettings, taskCallback);
    }

    @Override
    public void bootstrapNode(PoolSettings poolSettings, long nodeId, TaskCallback<NodeModel> taskCallback) {
        final NodeModel node = _getNodeModel(nodeId);
        taskExecutor.execute(bootstrapMachineTask, new BootstrapMachineConfig() {
            @Override
            public String getBootstrapScriptResourcePath() {
                return bootstrapScriptResourcePath;
            }
            @Override
            public NodeModel getNodeModel() {
                return node;
            }
        }, poolSettings, taskCallback);
    }

    @Override
    public List<ErrorModel> listTaskErrors(PoolSettings poolSettings) {
        if (poolSettings == null) return null;
        return errorsDao.readAllOfPool(poolSettings.getUuid());
    }

    @Override
    public ErrorModel getTaskError(long errorId) {
        return errorsDao.read(errorId);
    }

    @Override
    public void removeTaskError(long errorId) {
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
    public NodeModel occupy(PoolSettings poolSettings) {
        return nodesDao.occupyNode( poolSettings );
    }

    @Override
    public List<NodeMappings> listCloudNodes(PoolSettings poolSettings) {
        return nodeMappingsDao.readAll(poolSettings);
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

    public void setCreateMachineTask(Task createMachineTask) {
        this.createMachineTask = createMachineTask;
    }

    public void setDeleteMachineTask(Task deleteMachineTask) {
        this.deleteMachineTask = deleteMachineTask;
    }

    public void setBootstrapMachineTask(Task bootstrapMachineTask) {
        this.bootstrapMachineTask = bootstrapMachineTask;
    }

    public void setStatusManager(StatusManager statusManager) {
        this.statusManager = statusManager;
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setBootstrapScriptResourcePath(String bootstrapScriptResourcePath) {
        this.bootstrapScriptResourcePath = bootstrapScriptResourcePath;
    }
}
