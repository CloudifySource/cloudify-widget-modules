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
import java.util.LinkedList;
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

    /**
     * Checks if there is a need to create nodes within the minNodes limit
     *
     * @return
     */
    private Boolean shouldCreateMoreNodes() {
        Constraints constraints = getConstraints();
        LinkedList<NodeStatus> nodeStatuses = new LinkedList<NodeStatus>();
        nodeStatuses.add(NodeStatus.CREATING);
        nodeStatuses.add(NodeStatus.CREATED);
        nodeStatuses.add(NodeStatus.BOOTSTRAPPING);
        nodeStatuses.add(NodeStatus.BOOTSTRAPPED);

        logger.info("[shouldCreateNodes] Checking if should create nodes for pool [{}]", constraints.poolSettings.getUuid());
        List<NodeModel> nodeModels = nodesDao.readAllOfPoolWithStatusRange(constraints.poolSettings.getUuid(), nodeStatuses);
        int numInstancesInQueue = CollectionUtils.size(getOwnDecisionModelsQueue());    // nodes that are going to be bootstrapped soon.

        boolean shouldCreate = nodeModels.size() + numInstancesInQueue < constraints.minNodes;
        logger.info("[shouldCreateNodes] number of nodes in creation or bootstrap process: [{}]", nodeModels.size());
        logger.info("[shouldCreateNodes] number of decisions already in queue: [{}]", numInstancesInQueue);
        logger.info("[shouldCreateNodes] returned [{}]", shouldCreate);
        return shouldCreate;
    }

    /**
     * Check if there is room to create more nodes within the maxNodes limit
     *
     * @return
     */
    private Boolean canCreateMoreNodes() {
        Constraints constraints = getConstraints();
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(constraints.poolSettings.getUuid());    // all nodes in all states.

        boolean canCreate = nodeModels.size() < constraints.maxNodes;
        logger.info("[canCreateNodes] number of nodes for pool [{}]", nodeModels.size());
        logger.info("[canCreateNodes] returned [{}]", canCreate);
        return canCreate;
    }

    @Override
    public CreateNodeManagementModule decide() {
        Constraints constraints = getConstraints();
        logger.info("- deciding decisions on pool [{}]", constraints.poolSettings.getUuid());

        if (shouldCreateMoreNodes() && canCreateMoreNodes()) {
            DecisionModel decisionModel = buildOwnDecisionModel(new CreateDecisionDetails());
            decisionsDao.create(decisionModel);
        }

        return this;
    }

    @Override
    public CreateNodeManagementModule execute() {

        Constraints constraints = getConstraints();
        logger.info("- executing decisions on pool [{}]", constraints.poolSettings.getUuid());

        List<DecisionModel> decisionModelsQueue = getOwnDecisionModelsQueue();
        if (decisionModelsQueue == null || decisionModelsQueue.isEmpty()) {
            logger.info("CreateModule - no decisions to execute");
            return this;
        }
        logger.debug("CreateModule - found [{}] decisions", decisionModelsQueue.size());

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
