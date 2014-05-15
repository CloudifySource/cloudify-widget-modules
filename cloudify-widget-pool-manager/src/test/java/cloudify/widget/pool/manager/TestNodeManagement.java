package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.node_management.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: eliranm
 * Date: 4/25/14
 * Time: 10:01 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
public class TestNodeManagement {

    private static Logger logger = LoggerFactory.getLogger(TestNodeManagement.class);

    @Autowired
    private SettingsDataAccessManager settingsDataAccessManager;

    @Autowired
    private NodeManagementRunner nodeManagementRunner;

    @Autowired
    private CreateNodeManager createNodeManager;

    @Autowired
    private DecisionsDao decisionsDao;

    private ManagerSettings managerSettings;

    @Before
    public void setup() {
        managerSettings = settingsDataAccessManager.read();
        Assert.assertNotNull("manager settings should not be null", managerSettings);
    }

    @Test
    public void testDecisionMaker() throws IOException {

        createNodeManager.mode(NodeManager.Mode.MANUAL_APPROVAL);

        PoolSettings poolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.hp);
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

        PoolSettings poolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.hp);

        Set<String> machineIds = new HashSet<String>();
        machineIds.addAll(Arrays.asList("a", "b", "c"));

        DecisionModel createDecisionModel = new DecisionModel()
                .setDecisionType(DecisionType.CREATE)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setDetails(new CreateDecisionDetails()
                                .setNumInstances(3)
                );

        DecisionModel deleteDecisionModel = new DecisionModel()
                .setDecisionType(DecisionType.DELETE)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setDetails(new DeleteDecisionDetails()
                                .addMachineIds(machineIds)
                );

        DecisionModel prepareDecisionModel = new DecisionModel()
                .setDecisionType(DecisionType.PREPARE)
                .setPoolId(poolSettings.getUuid())
                .setApproved(false)
                .setDetails(new PrepareDecisionDetails()
                                .addMachineIds(machineIds)
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

        List<DecisionModel> decisionsWithTypeCreate = decisionsDao.readAllOfPoolWithDecisionType(poolSettings.getUuid(), DecisionType.CREATE);
        Assert.assertEquals("there should only be 1 decision with type 'create'", 1, decisionsWithTypeCreate.size());
        Assert.assertEquals("decision should be of type 'create'", DecisionType.CREATE, decisionsWithTypeCreate.iterator().next().decisionType);

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
