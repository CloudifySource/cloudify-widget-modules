package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.DecisionModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * User: eliranm
 * Date: 4/24/14
 * Time: 11:09 PM
 */
public abstract class BaseNodeManagementModule<T extends BaseNodeManagementModule> implements DecisionMaker<T>, ModuleTypeProvider {

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

    protected DecisionModel generateDecisionModel(DecisionDetails details) {
        return new DecisionModel()
                .setDecisionType(getType())
                .setPoolId(getConstraints().poolSettings.getUuid())
                .setApproved(NodeManagementMode.AUTO_APPROVAL == getConstraints().nodeManagementMode)
                .setDetails(details);
    }

    protected List<DecisionModel> getOwnDecisionModels() {
        return decisionsDao.readAllOfPoolWithDecisionType(getConstraints().poolSettings.getUuid(), getType());
    }

}
