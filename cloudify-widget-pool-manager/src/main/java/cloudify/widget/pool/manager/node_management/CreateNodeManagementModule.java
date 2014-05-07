package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
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
public class CreateNodeManagementModule extends BaseNodeManagementModule<CreateNodeManagementModule> {

    private static Logger logger = LoggerFactory.getLogger(CreateNodeManagementModule.class);

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Autowired
    private ErrorsDao errorsDao;

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
            // figure out how many machines we're intending to create
            for (DecisionModel decisionModel : decisionModels) {
                numInstancesInQueue += ((CreateDecisionDetails) decisionModel.details).getNumInstances();
            }
        }

        if (numInstancesInQueue + nodeModels.size() >= constraints.minNodes) {
            // no action needed, the queue will satisfy the constraints in the following iteration(s)
            return this;
        }


        DecisionModel decisionModel = createOwnDecisionModel(new CreateDecisionDetails()
                .setNumInstances(constraints.minNodes - nodeModels.size() - numInstancesInQueue));
        decisionsDao.create(decisionModel);

        return this;
    }

    @Override
    public CreateNodeManagementModule execute() {

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
                final CreateDecisionDetails details = (CreateDecisionDetails) decisionModel.details;
                int numInstances = details.getNumInstances();
                logger.debug("creating [{}] instances via pool manager task executor", numInstances);
                for (int i = 0; i < numInstances; i++) {
                    poolManagerApi.createNode(constraints.poolSettings, new TaskCallback<Collection<NodeModel>>() {

                        @Override
                        public void onSuccess(Collection<NodeModel> result) {
                            logger.debug("node created successfully, result is [{}]", result);
                            // it's the last node - remove the decision model
                            if (details.getNumInstances() == 1) {
                                decisionsDao.delete(decisionModel.id);
                                return;
                            }
                            // just decrement the number of instances to be created
                            decisionsDao.update(decisionModel.setDetails(details.decrementNumInstances()));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            logger.error("failed to create node", t);
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
        return NodeManagementModuleType.CREATE;
    }
}
