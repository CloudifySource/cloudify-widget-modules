package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
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
 * Date: 5/9/14
 * Time: 8:26 PM
 */
public class BootstrapNodeManagementModule extends BaseNodeManagementModule<BootstrapNodeManagementModule, BootstrapDecisionDetails> {

    private static Logger logger = LoggerFactory.getLogger(BootstrapNodeManagementModule.class);

    @Autowired
    private PoolManagerApi poolManagerApi;


    @Override
    public BootstrapNodeManagementModule decide() {
        Constraints constraints = getConstraints();
        List<Long> createdNodeIds = nodesDao.readIdsOfPoolWithStatus(constraints.poolSettings.getUuid(), NodeStatus.CREATED);

        // we have nothing to do if no created nodes found
        if (createdNodeIds.isEmpty()) {
            logger.debug("no created nodes to bootstrap");
            return this;
        }

        List<Long> createdNodeIdsInQueue = new LinkedList<Long>();

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // fetch all node ids we're intending to delete due to expiration
            for (DecisionModel decisionModel : decisionModels) {
                createdNodeIdsInQueue.addAll(((BootstrapDecisionDetails) decisionModel.details).getNodeIds());
            }
        }

        if (createdNodeIdsInQueue.containsAll(createdNodeIds)) {
            // no action needed, the created nodes will be handled in the following iteration(s)
            logger.info("created nodes are bootstrapped in the next iterations");
            return this;
        }

        // we only want the nodes that won't be handled in the queue
        createdNodeIds.removeAll(createdNodeIdsInQueue);

        logger.info("createdNodeIds [{}]", createdNodeIds);

        DecisionModel decisionModel = buildOwnDecisionModel(new BootstrapDecisionDetails()
                .setNodeIds(new HashSet<Long>(createdNodeIds)));
        decisionsDao.create(decisionModel);

        return this;
    }

    @Override
    public BootstrapNodeManagementModule execute() {
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
                final BootstrapDecisionDetails details = (BootstrapDecisionDetails) decisionModel.details;
                // copy to avoid concurrent modification
                Set<Long> toBootstrapIds = new HashSet<Long>(details.getNodeIds());
                logger.debug("bootstrapping [{}] instances via pool manager task executor", toBootstrapIds);
                for (final Long toBootstrapId : toBootstrapIds) {
                    poolManagerApi.bootstrapNode(constraints.poolSettings, toBootstrapId, new TaskCallback<NodeModel>() {

                        @Override
                        public void onSuccess(NodeModel result) {
                            logger.debug("node with id [{}] bootstrapped successfully", toBootstrapId);
                            // it's the last node - remove the decision model
                            if (details.getNodeIds().size() == 1) {
                                decisionsDao.delete(decisionModel.id);
                                return;
                            }
                            // just decrement the number of instances to be deleted
                            decisionsDao.update(decisionModel.setDetails(details.removeNodeId(toBootstrapId)));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            logger.error("failed to bootstrap node", t);
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
        return NodeManagementModuleType.BOOTSTRAP;
    }
}
