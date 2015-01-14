package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;

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
        // todo: spilt into two separate decisions - one that marks nodes as expired due to time passed and another that deletes expired nodes.
        Constraints constraints = getConstraints();
        logger.info("- deciding decisions on pool [{}]", constraints.poolSettings.getUuid());
        // get all nodes that should be expired
        List<Long> expiredNodeIds = nodesDao.readExpiredIdsOfPool(constraints.poolSettings.getUuid());

        // mark them with a status
        for (long expiredNodeId : expiredNodeIds) {
            nodesDao.updateStatus(expiredNodeId, NodeStatus.EXPIRED);
        }

        // now get all nodes with expired state ( list might be bigger than the ones we just marked)
        expiredNodeIds = nodesDao.readIdsOfPoolWithStatus(constraints.poolSettings.getUuid(), NodeStatus.EXPIRED);

        // we have nothing to do if no expired nodes found
        if (expiredNodeIds.isEmpty()) {
            logger.info("no expired nodes to delete");
            return this;
        }

        List<Long> expiredNodeIdsInQueue = new LinkedList<Long>();

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // fetch all node ids we're intending to delete due to expiration
            for (DecisionModel decisionModel : decisionModels) {
                expiredNodeIdsInQueue.add(((DeleteExpiredDecisionDetails) decisionModel.details).getNodeId());
            }
        }

        if (expiredNodeIdsInQueue.containsAll(expiredNodeIds)) {
            // no action needed, the expired nodes will be handled in the following iteration(s)
            logger.info("expired nodes are deleted in the next iterations");
            return this;
        }

        // we only want the nodes that won't be handled in the queue
        expiredNodeIds.removeAll(expiredNodeIdsInQueue);

        logger.info("expiredNodeIds [{}]", expiredNodeIds);

        for (Long expiredNodeId : expiredNodeIds) {
            DecisionModel decisionModel = buildOwnDecisionModel(new DeleteExpiredDecisionDetails()
                    .setNodeId(expiredNodeId));
            decisionsDao.create(decisionModel);
        }

        return this;
    }

    @Override
    public DeleteExpiredNodeManagementModule execute() {
        Constraints constraints = getConstraints();
        logger.info("- executing decisions on pool [{}]", constraints.poolSettings.getUuid());

        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels == null || decisionModels.isEmpty()) {
            logger.info("no decisions to execute");
            return this;
        }
        logger.info("found [{}] decisions", decisionModels.size());

        for (final DecisionModel decisionModel : decisionModels) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                // TODO avoid casting - used generics in model
                final DeleteExpiredDecisionDetails details = (DeleteExpiredDecisionDetails) decisionModel.details;
                final long toDeleteId = details.getNodeId();
                logger.info("deleting instance with id [{}] via pool manager task executor", toDeleteId);
                poolManagerApi.deleteNode(constraints.poolSettings, toDeleteId, new TaskCallback<Void>() {

                    @Override
                    public void onSuccess(Void result) {
                        teardownDecisionExecution(decisionModel);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        teardownDecisionExecution(decisionModel);
                    }
                });

                logger.info("task sent, marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
            }
        }

        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.DELETE_EXPIRED;
    }

    public void setPoolManagerApi(PoolManagerApi poolManagerApi) {
        this.poolManagerApi = poolManagerApi;
    }
}
