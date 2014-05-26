package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.ErrorModel;
import com.google.common.collect.Maps;
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

    @Autowired
    private ErrorsDao errorsDao;


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


    protected void teardownDecisionExecution(DecisionModel decisionModel) {
        decisionsDao.delete(decisionModel.id);
    }

    protected void writeError(Throwable t) {
        String message = t.getMessage();
        HashMap<String, Object> infoMap = Maps.newHashMap();
        infoMap.put("stackTrace", t.getStackTrace());
        logger.error(message);
        errorsDao.create(new ErrorModel()
                        .setSource(getClass().getSimpleName())
                        .setPoolId(getConstraints().poolSettings.getUuid())
                        .setMessage(message)
                        .setInfoFromMap(infoMap)
        );
    }


}
