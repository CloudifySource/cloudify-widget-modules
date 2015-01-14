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
        logger.info("- deciding decisions on pool [{}]", constraints.poolSettings.getUuid());
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
                createdNodeIdsInQueue.add(((BootstrapDecisionDetails) decisionModel.details).getNodeId());
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

        for (Long createdNodeId : createdNodeIds) {
            DecisionModel decisionModel = buildOwnDecisionModel(new BootstrapDecisionDetails()
                    .setNodeId(createdNodeId));
            decisionsDao.create(decisionModel);
        }

        return this;
    }

    @Override
    public BootstrapNodeManagementModule execute() {
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
                final BootstrapDecisionDetails details = (BootstrapDecisionDetails) decisionModel.details;
                final long toBootstrapId = details.getNodeId();
                logger.info("bootstrapping instance with id [{}] via pool manager task executor", toBootstrapId);

                poolManagerApi.bootstrapNode(constraints.poolSettings, toBootstrapId, new TaskCallback<NodeModel>() {

                    @Override
                    public void onSuccess(NodeModel result) {
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
        return NodeManagementModuleType.BOOTSTRAP;
    }
}
