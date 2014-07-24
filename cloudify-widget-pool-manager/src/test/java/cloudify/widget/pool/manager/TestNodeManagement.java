package cloudify.widget.pool.manager;

import cloudify.widget.common.DatabaseBuilder;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.node_management.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.List;

/**
 * User: eliranm
 * Date: 4/25/14
 * Time: 10:01 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
public class TestNodeManagement {

    private static Logger logger = LoggerFactory.getLogger(TestNodeManagement.class);
    private static final String SCHEMA = "node_manager_test";
    private static final String SQL_PATH = "sql";

    @Autowired
    private SettingsDataAccessManager settingsDataAccessManager;

    @Autowired
    private NodeManagementExecutor nodeManagementExecutor;

    @Autowired
    private CreateNodeManagementModule createNodeManager;

    @Autowired
    private DecisionsDao decisionsDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ManagerSettings managerSettings;

    @Before
    public void init() {
        managerSettings = settingsDataAccessManager.read();
        Assert.assertNotNull("manager settings should not be null", managerSettings);

        // initializing test schema
        DatabaseBuilder.buildDatabase(jdbcTemplate, SCHEMA, SQL_PATH);

    }

    @After
    public void destroy() {
        DatabaseBuilder.destroyDatabase(jdbcTemplate, SCHEMA);
    }


    @Test
    public void testDecisionMaker() throws IOException {

        PoolSettings poolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.hpFolsom);
        logger.info("- pool settings [{}]", poolSettings.getProvider().getName());

        for (int i = 0; i < 3; i++) {

            createNodeManager
                    .having(new Constraints(poolSettings))
                    .decide()
                    .execute();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void testDecisionsDao() {

        PoolSettings poolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.hpFolsom);

        long nodeId = 1L;

        DecisionModel createDecisionModel = new DecisionModel()
                .setDecisionType(NodeManagementModuleType.CREATE)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setExecuted(false)
                .setDetails(new CreateDecisionDetails());

        DecisionModel deleteDecisionModel = new DecisionModel()
                .setDecisionType(NodeManagementModuleType.DELETE)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setExecuted(false)
                .setDetails(new DeleteDecisionDetails()
                                .setNodeId(nodeId)
                );

        DecisionModel prepareDecisionModel = new DecisionModel()
                .setDecisionType(NodeManagementModuleType.BOOTSTRAP)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setExecuted(false)
                .setDetails(new BootstrapDecisionDetails()
                                .setNodeId(nodeId)
                );

        // create

        boolean createModelCreated = decisionsDao.create(createDecisionModel);
        boolean deleteModelCreated = decisionsDao.create(deleteDecisionModel);
        boolean prepareModelCreated = decisionsDao.create(prepareDecisionModel);

        logger.info("created models: create [{}] delete [{}] prepare [{}]", createModelCreated, deleteModelCreated, prepareModelCreated);

        Assert.assertTrue(
                String.format("failed to create models: create [%s] delete [%s] prepare [%s]", createModelCreated, deleteModelCreated, prepareModelCreated),
                createModelCreated && deleteModelCreated && prepareModelCreated);

        // read

        DecisionModel readPrepareDecisionModel = decisionsDao.read(prepareDecisionModel.id);
        Assert.assertNotNull(readPrepareDecisionModel);
        Assert.assertEquals("model ids should be equal", prepareDecisionModel.id, readPrepareDecisionModel.id);

        List<DecisionModel> decisionsOfPool = decisionsDao.readAllOfPool(poolSettings.getUuid());
        Assert.assertEquals("decisions of pool should have a size of 3", 3, decisionsOfPool.size());

        List<DecisionModel> decisionsWithTypeCreate = decisionsDao.readAllOfPoolWithDecisionType(poolSettings.getUuid(), NodeManagementModuleType.CREATE);
        Assert.assertEquals("there should only be 1 decision with type 'create'", 1, decisionsWithTypeCreate.size());
        Assert.assertEquals("decision should be of type 'create'", NodeManagementModuleType.CREATE, decisionsWithTypeCreate.iterator().next().decisionType);

        // update

        decisionsDao.update(createDecisionModel.setApproved(true));
        DecisionModel readCreateDecisionModel = decisionsDao.read(createDecisionModel.id);
        logger.info("updated create decision model 'approved' to [{}]", readCreateDecisionModel.approved);

        Assert.assertTrue("model 'approved' should be updated to 'true'", readCreateDecisionModel.approved);

        // delete
        decisionsDao.delete(createDecisionModel.id);
        decisionsDao.delete(deleteDecisionModel.id);
        decisionsDao.delete(prepareDecisionModel.id);

        DecisionModel deletedCreateDecisionModel = decisionsDao.read(createDecisionModel.id);
        DecisionModel deletedDeleteDecisionModel = decisionsDao.read(deleteDecisionModel.id);
        DecisionModel deletedPrepareDecisionModel = decisionsDao.read(prepareDecisionModel.id);

        logger.info("deleted models: create [{}] delete [{}] prepare [{}]", deletedCreateDecisionModel, deletedDeleteDecisionModel, deletedPrepareDecisionModel);

        Assert.assertNull(deletedCreateDecisionModel);
        Assert.assertNull(deletedDeleteDecisionModel);
        Assert.assertNull(deletedPrepareDecisionModel);
    }


    public void setSettingsDataAccessManager(SettingsDataAccessManager settingsDataAccessManager) {
        this.settingsDataAccessManager = settingsDataAccessManager;
    }

}
