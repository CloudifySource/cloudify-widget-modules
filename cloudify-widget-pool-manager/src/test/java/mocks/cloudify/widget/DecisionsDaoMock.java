package mocks.cloudify.widget;

import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.node_management.DecisionsDao;
import cloudify.widget.pool.manager.node_management.NodeManagementModuleType;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/30/14
 * Time: 9:39 PM
 */
public class DecisionsDaoMock extends DecisionsDao {

    public List<DecisionModel> db = new LinkedList<DecisionModel>();

    @Override
    public List<DecisionModel> readAllOfPoolWithDecisionType(String poolId, NodeManagementModuleType nodeManagementModuleType) {
        List<DecisionModel> result = new LinkedList<DecisionModel>();

        for (DecisionModel decisionModel : db) {
            if ( decisionModel.decisionType == nodeManagementModuleType ){
                result.add(decisionModel);
            }
        }

        return result;
    }

    @Override
    public boolean create(DecisionModel decisionModel) {
        db.add(decisionModel);
        return true;
    }

    @Override
    public int update(DecisionModel decisionModel) {
        return 1;
    }
}
