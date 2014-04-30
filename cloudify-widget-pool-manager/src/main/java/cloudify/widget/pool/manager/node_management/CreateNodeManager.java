package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.DecisionType;
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
public class CreateNodeManager extends NodeManager<CreateNodeManager> {

    private static Logger logger = LoggerFactory.getLogger(CreateNodeManager.class);

    private CreateDecisionDetails _details;

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Autowired
    private ErrorsDao errorsDao;

    @Override
    public CreateNodeManager decide() {
        logger.info("deciding...");

        Constraints constraints = getConstraints();
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(constraints.poolSettings.getUuid());

        // we create more machines only if existing machines are less than the minimum
        if (constraints.minNodes <= nodeModels.size()) {
            return this;
        }

        int numInstancesSum = 0;

        // TODO refactor - extract to actionNeeded() -> boolean or something
        // check if there are decisions in the queue, and if executing them will satisfy the constraints
        List<DecisionModel> decisionModels = decisionsDao.readAllOfPoolWithDecisionType(
                constraints.poolSettings.getUuid(), DecisionType.CREATE);
        if (decisionModels != null && !decisionModels.isEmpty()) {
            // figure out how many machines we're intending to create
            for (DecisionModel decisionModel : decisionModels) {
                numInstancesSum += ((CreateDecisionDetails) decisionModel.details).getNumInstances();
            }
            if (numInstancesSum + nodeModels.size() >= constraints.minNodes) {
                // no action needed, the queue will satisfy the constraints in the following iteration(s)
                return this;
            }
        }


        _details = new CreateDecisionDetails()
                .setNumInstances(constraints.minNodes - nodeModels.size() - numInstancesSum);

        DecisionModel decisionModel = new DecisionModel()
                .setDecisionType(DecisionType.CREATE)
                .setPoolId(constraints.poolSettings.getUuid())
                .setApproved(NodeManagerMode.AUTO_APPROVAL == constraints.nodeManagerMode)
                .setDetails(_details);

        decisionsDao.create(decisionModel);

        return this;
    }

    @Override
    public CreateNodeManager execute() {
        logger.info("executing...");

        Constraints constraints = getConstraints();

        List<DecisionModel> decisionModels = decisionsDao.readAllOfPoolWithDecisionType(
                constraints.poolSettings.getUuid(), DecisionType.CREATE);
        if (decisionModels == null || decisionModels.isEmpty()) {
            logger.info("no decisions to execute");
            return this;
        }

        logger.info("found [{}] decisions", decisionModels.size());
        for (final DecisionModel decisionModel : decisionModels) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                // TODO prevent casting - used generics in model
                int numInstances = ((CreateDecisionDetails) decisionModel.details).getNumInstances();
                logger.info("creating [{}] instances via pool manager task executor", numInstances);
                for (int i = 0; i < numInstances; i++) {
                    poolManagerApi.createNode(constraints.poolSettings, new TaskCallback<Collection<NodeModel>>() {

                        @Override
                        public void onSuccess(Collection<NodeModel> result) {
                            logger.info("machine created successfully, result is [{}]", result);
                            // it's the last machine - remove the model
                            if (((CreateDecisionDetails) decisionModel.details).getNumInstances() == 1) {
                                decisionsDao.delete(decisionModel.id);
                                return;
                            }
                            // just decrement the number of instances to be created
                            decisionsDao.update(decisionModel.setDetails(_details.decrementNumInstances()));
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            logger.error("failed to create machine", t);
                        }
                    });
                }

                logger.info("task sent, marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
            }
        }

        return this;
    }

}
