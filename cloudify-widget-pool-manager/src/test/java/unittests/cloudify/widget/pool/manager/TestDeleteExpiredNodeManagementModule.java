package unittests.cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.PoolManagerApiImpl;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeManagementSettings;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.node_management.Constraints;
import cloudify.widget.pool.manager.node_management.DeleteExpiredDecisionDetails;
import cloudify.widget.pool.manager.node_management.DeleteExpiredNodeManagementModule;
import cloudify.widget.pool.manager.node_management.NodeManagementModuleType;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import junit.framework.Assert;
import mocks.cloudify.widget.DecisionsDaoMock;
import mocks.cloudify.widget.NodesDaoMock;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/30/14
 * Time: 9:29 PM
 */
public class TestDeleteExpiredNodeManagementModule {

    private static Logger logger = LoggerFactory.getLogger(TestDeleteExpiredNodeManagementModule.class);

    @Test

    public void testDecision() {
        DeleteExpiredNodeManagementModule module = new DeleteExpiredNodeManagementModule();

        module.setNodesDao(new NodesDaoMock());
        DecisionsDaoMock decisionsDao = new DecisionsDaoMock();
        module.setDecisionsDao(decisionsDao);

        PoolSettings ps = new PoolSettings();

        ps.setMinNodes(5);
        ps.setMaxNodes(10);
        ps.setNodeManagement(new NodeManagementSettings());

        Constraints constraints = new Constraints(ps);


        module.having(constraints);
        module.decide();

        logger.info("decision db size [{}]", decisionsDao.db.size());
        Assert.assertEquals("there should be a new 'delete expired' decision when there's a node with expired status in db", 1, decisionsDao.db.size());




    }


    @Test
    public void testExecute(){
        DeleteExpiredNodeManagementModule module = new DeleteExpiredNodeManagementModule();

        module.setNodesDao(new NodesDaoMock());
        DecisionsDaoMock decisionsDao = new DecisionsDaoMock();
        module.setDecisionsDao(decisionsDao);

        PoolSettings ps = new PoolSettings();

        ps.setMinNodes(5);
        ps.setMaxNodes(10);
        ps.setNodeManagement(new NodeManagementSettings());

        Constraints constraints = new Constraints(ps);


        module.having(constraints);


        PoolManagerApiMock poolManagerApi = new PoolManagerApiMock();
        module.setPoolManagerApi(poolManagerApi);

        DecisionModel decisionModel = new DecisionModel();
        decisionModel.approved = true;
        decisionModel.decisionType = NodeManagementModuleType.DELETE_EXPIRED;
        DeleteExpiredDecisionDetails deleteExpiredDecisionDetails = new DeleteExpiredDecisionDetails();
        deleteExpiredDecisionDetails.setNodeId(1234);
        decisionModel.details = deleteExpiredDecisionDetails;

        decisionsDao.create(decisionModel);

        module.execute();

        Assert.assertTrue("delete node should be invoked when executing delete expired decision", poolManagerApi.deleteNodeInvoked);

        Assert.assertTrue("decision should be marked as 'executed' when it is executed",decisionModel.executed);
    }

    public static class PoolManagerApiMock extends PoolManagerApiImpl{
        public boolean deleteNodeInvoked = false;
        @Override
        public void deleteNode(PoolSettings poolSettings, long nodeId, TaskCallback<Void> taskCallback) {
            deleteNodeInvoked = true;
        }
    }
}
