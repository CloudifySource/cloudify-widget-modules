package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.tasks.TaskCallback;

import java.util.Collection;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/10/14
 * Time: 5:39 PM
 */
public interface PoolManagerApi {

    PoolStatus getStatus(PoolSettings poolSettings);

    Collection<PoolStatus> listStatuses();

    List<NodeModel> listNodes(PoolSettings poolSettings);

    NodeModel getNode(long nodeId);

    /**
     *
     * @param poolSettings
     * @param taskCallback (optional)
     */
    void createNode(PoolSettings poolSettings, TaskCallback<Collection<NodeModel>> taskCallback);

    /**
     *
     * @param poolSettings
     * @param nodeId
     * @param taskCallback (optional)
     */
    void deleteNode(PoolSettings poolSettings, long nodeId, TaskCallback<Void> taskCallback);

    /**
     *
     * @param poolSettings
     * @param nodeId
     * @param taskCallback (optional)
     */
    void bootstrapNode(PoolSettings poolSettings, long nodeId, TaskCallback<NodeModel> taskCallback);

    PingResult pingNode(PoolSettings poolSettings, long nodeId);

    void expireNode(PoolSettings poolSettings, long nodeId);

    List<ErrorModel> listErrors(PoolSettings poolSettings);

    void deleteErrors(PoolSettings poolSettings);

    ErrorModel getError(long errorId);

    void deleteError(long errorId);

    List<TaskModel> listRunningTasks(PoolSettings poolSettings);

    void removeRunningTask(long taskId);

    NodeModel occupy( PoolSettings poolSettings, long expires );

    List<NodeMappings> listCloudNodes (PoolSettings poolSettings);

    List<DecisionModel> listDecisions(PoolSettings poolSettings);

    void abortDecision(PoolSettings poolSettings, long decisionId);

    void updateDecisionApproval(PoolSettings poolSettings, long decisionId, boolean approved);

    /**
     * Deletes all bootstrapped nodes from the pool.
     *
     * @param poolSettings
     */
    void cleanPool(PoolSettings poolSettings);

}
