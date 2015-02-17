package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApiImpl;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by sefi on 9/11/14.
 */
public class PingNodeManagementModule extends BaseNodeManagementModule<PingNodeManagementModule, PingDecisionDetails> {

    private static final Logger logger = LoggerFactory.getLogger(PingNodeManagementModule.class);

    @Autowired
    private PoolManagerApiImpl poolManagerApi;


    @Override
    public PingNodeManagementModule decide() {
        Constraints constraints = getConstraints();
        logger.info("- deciding decisions on pool [{}]", constraints.poolSettings.getUuid());

        List<NodeModel> bootstrappedNodeModels = nodesDao.readAllOfPoolWithStatus(constraints.poolSettings.getUuid(), NodeStatus.BOOTSTRAPPED);

        // we have nothing to do if no bootstrapped nodes found
        if (bootstrappedNodeModels.isEmpty()) {
            logger.info("no bootstrapped nodes to ping");
            return this;
        }

        int nodesInQueue = 0;

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // how many machines we're intending to delete
            nodesInQueue += decisionModels.size();
        }

        if (bootstrappedNodeModels.size() - nodesInQueue <= 0) {
            // no action needed, the queue will satisfy the constraints in the following iteration(s)
            return this;
        }

        Set<Long> toPingIds = _collectNodesToPing(bootstrappedNodeModels, decisionModels);
        logger.info("toPingIds [{}]", toPingIds);

        for (Long toPingId : toPingIds) {
            logger.info("Pinging node [{}]", toPingId);
            PingResult pingResult = poolManagerApi.pingNode(constraints.poolSettings, toPingId);

            if (pingResult.getPingStatus() == PingStatus.PING_FAIL) {
                logger.info("Ping for node [{}] failed, creating decision to mark it as MARK_EXPIRED_PING", toPingId);
                // todo: add PingResult details to decision details.
                DecisionModel decisionModel = buildOwnDecisionModel(new PingDecisionDetails().setNodeId(toPingId).setPingResult(pingResult));
                decisionsDao.create(decisionModel);
            }

        }

        return this;
    }

    private Set<Long> _collectNodesToPing(List<NodeModel> nodeModels, List<DecisionModel> decisionModels) {
        Set<Long> toPingNodeIds = new HashSet<Long>();

        // go through all nodeModels
        for (NodeModel nodeModel : nodeModels) {
            if (decisionModels.isEmpty()) {
                toPingNodeIds.add(nodeModel.id);
                continue;
            }

            // check if it already has a decision
            boolean found = false;
            for (DecisionModel decisionModel : decisionModels) {
                PingDecisionDetails pdd = (PingDecisionDetails) decisionModel.details;
                if (nodeModel.id == pdd.getNodeId()) {
                    // there's already a decision for this node, move on to the next one.
                    found = true;
                }
            }
            if (!found) {
                toPingNodeIds.add(nodeModel.id);
            }
        }

        return toPingNodeIds;
    }

    @Override
    public PingNodeManagementModule execute() {
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
                final PingDecisionDetails details = (PingDecisionDetails) decisionModel.details;
                // copy to avoid concurrent modification
                final long toPingId = details.getNodeId();
//                PingResult pingResult = details.getPingResult();

                logger.info("Marking instance with id [{}] as EXPIRED", toPingId);
                // todo: change this to update status by id and status == BOOTSTRAPPED
                nodesDao.updateStatus(toPingId, NodeStatus.EXPIRED);

                logger.info("marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
                teardownDecisionExecution(decisionModel);
            }
        }

        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.MARK_EXPIRED_PING;
    }
}