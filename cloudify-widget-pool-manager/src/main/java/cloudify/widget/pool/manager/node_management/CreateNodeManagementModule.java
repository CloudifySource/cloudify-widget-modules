package cloudify.widget.pool.manager.node_management;

import cloudify.widget.common.CollectionUtils;
import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class CreateNodeManagementModule extends BaseNodeManagementModule<CreateNodeManagementModule, CreateDecisionDetails> {

    private static Logger logger = LoggerFactory.getLogger(CreateNodeManagementModule.class);

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Override
    public CreateNodeManagementModule decide() {

        Constraints constraints = getConstraints();
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(constraints.poolSettings.getUuid());
        List<NodeModel> bootstrappedModels = nodesDao.readAllOfPoolWithStatus(constraints.poolSettings.getUuid(), NodeStatus.BOOTSTRAPPED);
        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        int numInstancesInQueue = CollectionUtils.size(getOwnDecisionModelsQueue());
        int needed = Math.min(
                constraints.minNodes - bootstrappedModels.size() - numInstancesInQueue, // how many do I need for a minimum pool size
                constraints.maxNodes - nodeModels.size() ); // how many can I add

        logger.debug("{ 'minNodes' : {} , 'maxNodes' :{}, 'allNodes' :{}, 'bootstrappedModels':{}, 'inQueue':{},'needed':{} }", constraints.minNodes,
                constraints.maxNodes,
                nodeModels.size(),
                bootstrappedModels.size(),
                numInstancesInQueue,
                needed
                );

        // we create more machines only if existing machines are less than the minimum
        if (nodeModels.size() >= constraints.maxNodes || bootstrappedModels.size() >= constraints.minNodes ) {
            logger.debug("I have enough machines. I don't need more. ");
            return this;
        }

        if (numInstancesInQueue + nodeModels.size() >= constraints.maxNodes || numInstancesInQueue + bootstrappedModels.size() >= constraints.minNodes ) {
            logger.debug("I might not have enough machines now, but I am going too.. so I don't need more.");
            // no action needed, the queue will satisfy the constraints in the following iteration(s)
            return this;
        }


        for (int i = 0; i < needed; i++) {
            DecisionModel decisionModel = buildOwnDecisionModel(new CreateDecisionDetails());
            decisionsDao.create(decisionModel);
        }

        return this;
    }

    @Override
    public CreateNodeManagementModule execute() {

        Constraints constraints = getConstraints();
        logger.info("- executing decision on pool [{}]", constraints.poolSettings.getUuid());

        List<DecisionModel> decisionModelsQueue = getOwnDecisionModelsQueue();
        if (decisionModelsQueue == null || decisionModelsQueue.isEmpty()) {
            logger.info("no decisions to execute");
            return this;
        }
        logger.debug("found [{}] decisions", decisionModelsQueue.size());

        for (final DecisionModel decisionModel : decisionModelsQueue) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                logger.debug("creating machine instance via pool manager task executor");
                poolManagerApi.createNode(constraints.poolSettings, new TaskCallback<Collection<NodeModel>>() {

                    @Override
                    public void onSuccess(Collection<NodeModel> result) {
                        teardownDecisionExecution(decisionModel);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        teardownDecisionExecution(decisionModel);
                    }
                });

                logger.debug("task sent, marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
            }
        }

        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.CREATE;
    }
}
