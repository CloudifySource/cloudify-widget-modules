package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class DeleteExpiredNodeManagementModule extends BaseNodeManagementModule<DeleteExpiredNodeManagementModule, DeleteExpiredDecisionDetails> {

    private static Logger logger = LoggerFactory.getLogger(DeleteExpiredNodeManagementModule.class);

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Override
    public DeleteExpiredNodeManagementModule decide() {
        Constraints constraints = getConstraints();
        List<Long> expiredNodeIds = nodesDao.readExpiredIdsOfPool(constraints.poolSettings.getUuid());

        // we have nothing to do if no expired nodes found
        if (expiredNodeIds.isEmpty()) {
            logger.debug("no expired nodes to delete");
            return this;
        }

        List<Long> expiredNodeIdsInQueue = new LinkedList<Long>();

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // fetch all node ids we're intending to delete due to expiration
            for (DecisionModel decisionModel : decisionModels) {
                expiredNodeIdsInQueue.addAll(((DeleteExpiredDecisionDetails) decisionModel.details).getNodeIds());
            }
        }

        if (expiredNodeIdsInQueue.containsAll(expiredNodeIds)) {
            // no action needed, the expired nodes will be handled in the following iteration(s)
            logger.info("expired nodes are deleted in the next iterations");
            return this;
        }

        // we only want the nodes that won't be handled in the queue
        expiredNodeIds.removeAll(expiredNodeIdsInQueue);

        // mark expired nodes with a status
        for (long expiredNodeId : expiredNodeIds) {
            nodesDao.setExpired(expiredNodeId);
        }

        logger.info("expiredNodeIds [{}]", expiredNodeIds);

        DecisionModel decisionModel = createOwnDecisionModel(new DeleteExpiredDecisionDetails()
                .setNodeIds(new HashSet<Long>(expiredNodeIds)));
        decisionsDao.create(decisionModel);

        return this;
    }

    @Override
    public DeleteExpiredNodeManagementModule execute() {
        Constraints constraints = getConstraints();

        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels == null || decisionModels.isEmpty()) {
            logger.info("no decisions to execute");
            return this;
        }
        logger.debug("found [{}] decisions", decisionModels.size());

        for (final DecisionModel decisionModel : decisionModels) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                // TODO avoid casting - used generics in model
                final DeleteExpiredDecisionDetails details = (DeleteExpiredDecisionDetails) decisionModel.details;
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
        return NodeManagementModuleType.DELETE_EXPIRED;
    }
}
