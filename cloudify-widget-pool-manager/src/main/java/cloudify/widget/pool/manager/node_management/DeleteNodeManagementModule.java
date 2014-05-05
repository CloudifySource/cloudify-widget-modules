package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 3:27 PM
 */
public class DeleteNodeManagementModule extends BaseNodeManagementModule<DeleteNodeManagementModule> {

    private static Logger logger = LoggerFactory.getLogger(DeleteNodeManagementModule.class);

    @Autowired
    private PoolManagerApi poolManagerApi;


/*
    @Autowired
    private StatusManager statusManager;

    @Autowired
    private ErrorsDao errorsDao;
*/
/*
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
*/

    @Override
    public DeleteNodeManagementModule decide() {

        Constraints constraints = getConstraints();
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(constraints.poolSettings.getUuid());

        // we delete machines only if nodes in pool exceed the maximum
        if (nodeModels.size() <= constraints.maxNodes) {
            return this;
        }

        int nodesInQueue = 0;

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModels();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // figure out how many machines we're intending to delete
            for (DecisionModel decisionModel : decisionModels) {
                nodesInQueue += ((DeleteDecisionDetails) decisionModel.details).getNodeIds().size();
            }
            if (nodeModels.size() - nodesInQueue <= constraints.maxNodes) {
                // no action needed, the queue will satisfy the constraints in the following iteration(s)
                return this;
            }
        }


//        Set<String> expiredIds = new HashSet<String>(nodesDao.readIdsOfPoolWithNodeStatus(constraints.poolSettings.getUuid(), NodeStatus.EXPIRED));

        // collect nodes for deletion
        Set<Long> toDeleteIds = _collectNodesToDelete(nodeModels, nodeModels.size() - nodesInQueue - constraints.maxNodes);
        logger.info("toDeleteIds [{}]", toDeleteIds);

        DecisionModel decisionModel = generateDecisionModel(new DeleteDecisionDetails().setNodeIds(toDeleteIds));
        decisionsDao.create(decisionModel);

        return this;
    }

    private Set<Long> _collectNodesToDelete(List<NodeModel> nodeModels, int target) {
        // incrementally collect nodes starting with the least destructive status
        Set<Long> toDeleteNodeIds = new HashSet<Long>();
        // go through all statuses
        for (NodeStatus nodeStatus : NodeStatus.values()) {
            for (NodeModel nodeModel : nodeModels) {
                // only get nodes of a certain status
                if (nodeModel.nodeStatus == nodeStatus) {
                    toDeleteNodeIds.add(nodeModel.id);
                    if (toDeleteNodeIds.size() == target) {
                        // we have got what we need, return
                        return toDeleteNodeIds;
                    }
                }
            }
        }

        return toDeleteNodeIds;
    }

    @Override
    public DeleteNodeManagementModule execute() {

        Constraints constraints = getConstraints();

        List<DecisionModel> decisionModels = getOwnDecisionModels();
        if (decisionModels == null || decisionModels.isEmpty()) {
            logger.info("no decisions to execute");
            return this;
        }
        logger.debug("found [{}] decisions", decisionModels.size());

        for (final DecisionModel decisionModel : decisionModels) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                // TODO avoid casting - used generics in model
                final DeleteDecisionDetails details = (DeleteDecisionDetails) decisionModel.details;
                // copy to avoid concurrent modification
                Set<Long> toDeleteIds = new HashSet<Long>(details.getNodeIds());
                logger.debug("deleting [{}] instances via pool manager task executor", toDeleteIds);
                for (final Long toDeleteId : toDeleteIds) {
                    poolManagerApi.deleteNode(constraints.poolSettings, toDeleteId, new TaskCallback<Void>() {

                        @Override
                        public void onSuccess(Void result) {
                            logger.debug("node with id [{}] deleted successfully", toDeleteId);
                            // it's the last node - remove the decision model
                            if (details.getNodeIds().size() == 1) {
                                decisionsDao.delete(decisionModel.id);
                                return;
                            }
                            // just decrement the number of instances to be deleted
                            decisionsDao.update(decisionModel.setDetails(details.removeNodeId(toDeleteId)));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            logger.error("failed to delete node", t);
                        }
                    });
                }

                logger.debug("task sent, marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
            }
        }

        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.DELETE;
    }
}
