package cloudify.widget.pool.manager.node_management;

import cloudify.widget.mailer.Mail;
import cloudify.widget.mailer.Mailer;
import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.EmailSettings;
import cloudify.widget.pool.manager.dto.ErrorModel;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;

/**
 * User: eliranm
 * Date: 4/24/14
 * Time: 11:09 PM
 */
public abstract class BaseNodeManagementModule<T extends BaseNodeManagementModule, D extends DecisionDetails> implements DecisionMaker<T>, ModuleTypeProvider {

    private static Logger logger = LoggerFactory.getLogger(BaseNodeManagementModule.class);

    @Autowired
    protected DecisionsDao decisionsDao;

    @Autowired
    protected NodesDao nodesDao;

    private Constraints _constraints;

    public BaseNodeManagementModule having(Constraints constraints) {
        _constraints = constraints;
        return this;
    }

    public Constraints getConstraints() {
        if (_constraints == null) {
            throw new RuntimeException("no constraints found!");
        }
        return _constraints;
    }

    protected void executeDecision( DecisionModel dm ){
        throw new UnsupportedOperationException("type " + getType() + " does not implement execute decision");
    }

    @Override
    public T execute() {

        Constraints constraints = getConstraints();
        logger.info("- executing decisions on pool [{}]", constraints.poolSettings.getUuid());

        List<DecisionModel> decisionModelsQueue = getOwnDecisionModelsQueue();
        if (decisionModelsQueue == null || decisionModelsQueue.isEmpty()) {
            logger.info(getType() + " : no decisions to execute");
            return (T) this;
        }
        logger.info(getType() + " - found [{}] decisions", decisionModelsQueue.size());

        for (final DecisionModel decisionModel : decisionModelsQueue) {
            logger.info("decision [{}], approved [{}], executed [{}]", decisionModel.id, decisionModel.approved, decisionModel.executed);

            if (decisionModel.approved && !decisionModel.executed) {

                executeDecision( decisionModel );

                logger.info("task sent, marking decision as executed");
                decisionsDao.update(decisionModel.setExecuted(true));
            }
        }

        return (T) this;
    }

    protected DecisionModel buildOwnDecisionModel(D details) {
        return new DecisionModel()
                .setDecisionType(getType())
                .setPoolId(getConstraints().poolSettings.getUuid())
                .setApproved(NodeManagementMode.AUTO_APPROVAL == getConstraints().nodeManagementMode)
                .setDetails(details);
    }

    protected List<DecisionModel> getOwnDecisionModelsQueue() {
        return decisionsDao.readAllOfPoolWithDecisionType(getConstraints().poolSettings.getUuid(), getType());
    }

    public void setNodesDao(NodesDao nodesDao) {
        this.nodesDao = nodesDao;
    }

    public void setDecisionsDao(DecisionsDao decisionsDao) {
        this.decisionsDao = decisionsDao;
    }

    protected void teardownDecisionExecution(DecisionModel decisionModel) {
        decisionsDao.delete(decisionModel.id);
    }

}
