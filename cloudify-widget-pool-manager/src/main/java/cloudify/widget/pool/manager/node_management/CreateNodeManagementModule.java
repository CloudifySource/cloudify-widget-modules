package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeModel;
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

        // we create more machines only if existing machines are less than the minimum
        if (nodeModels.size() >= constraints.minNodes) {
            return this;
        }

        int numInstancesInQueue = 0;

        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = getOwnDecisionModelsQueue();
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // how many machines we're intending to create
            numInstancesInQueue += decisionModels.size();
        }

        if (numInstancesInQueue + nodeModels.size() >= constraints.minNodes) {
            // no action needed, the queue will satisfy the constraints in the following iteration(s)
            return this;
        }

        int numInstances = constraints.minNodes - nodeModels.size() - numInstancesInQueue;
        for (int i = 0; i < numInstances; i++) {
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
                        writeError(t);
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
