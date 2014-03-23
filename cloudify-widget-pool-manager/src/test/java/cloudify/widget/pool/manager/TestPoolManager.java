package cloudify.widget.pool.manager;

import cloudify.widget.common.CollectionUtils;
import cloudify.widget.common.FileUtils;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.pool.manager.tasks.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.util.Assert;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/21/14
 * Time: 4:42 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
@ActiveProfiles({"softlayer", "dev"})
public class TestPoolManager {

    private static Logger logger = LoggerFactory.getLogger(TestPoolManager.class);

    private static final String SCHEMA = "pool_manager_test";

    @Autowired
    private PoolManagerApi poolManagerApi;

    @Autowired
    private NodesDao nodesDao;

    @Autowired
    private SettingsDataAccessManager settingsDataAccessManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskExecutor testTaskExecutor;

    @Autowired
    private Integer nExecutions;

    @Autowired
    private String bootstrapScriptResourcePath;


    @Before
    public void init() {

        // initializing test schema
        jdbcTemplate.update("create schema " + SCHEMA);
        jdbcTemplate.update("use " + SCHEMA);
        buildDatabase();

    }

    @After
    public void destroy() {
        jdbcTemplate.update("drop schema " + SCHEMA);
    }


    @Test
    public void test() {
        Assert.notNull(jdbcTemplate);
    }


    @Test(timeout = 10 * 60 * 1000)
    public void testTaskExecutor() {
        logger.info("testing manager task executor");


        PoolSettings softlayerPoolSettings = settingsDataAccessManager.read().getPools().getByProviderName(ProviderSettings.ProviderName.softlayer);
        logger.info("got pool settings with id [{}]", softlayerPoolSettings.getUuid());

        // delete - (should fail)

        logger.info("executing delete machine task that's bound to fail...");
        testTaskExecutor.execute(DeleteMachine.class, null, softlayerPoolSettings, null);

        List<ErrorModel> errorModels = poolManagerApi.listTaskErrors(softlayerPoolSettings);
        ErrorModel errorModel = CollectionUtils.first(errorModels);
        Assert.notNull(errorModel, "task error should not be null");
        logger.info("task message is [{}]", errorModel.message);
        Assert.isTrue(errorModel.taskName == TaskName.DELETE_MACHINE, "task name should be " + TaskName.DELETE_MACHINE);

        // create

        logger.info("executing create machine task [{}] times...", nExecutions);
        for (int i = 0; i < nExecutions; i++) {
            testTaskExecutor.execute(CreateMachine.class, null, softlayerPoolSettings, new TaskCallback() {
                @Override
                public void onSuccess(Object result) {
                    // TODO
                }

                @Override
                public void onFailure(Throwable t) {
                }
            });
        }

        logger.info("checking table for added node...");
        NodeModel nodeModel = null;
        List<NodeModel> softlayerNodeModels = poolManagerApi.listNodes(softlayerPoolSettings);
        for (NodeModel softlayerNodeModel : softlayerNodeModels) {
            nodeModel = softlayerNodeModel;
            logger.info("found node [{}]", nodeModel);
            break;
        }

        Assert.isTrue(nodeModel != null, "node cannot be null after machine is created");

        logger.info("after create machine, node status is [{}]", nodeModel.nodeStatus);
        Assert.isTrue(nodeModel.nodeStatus == NodeStatus.CREATED,
                String.format("node status should be [%s]", NodeStatus.CREATED));

        final NodeModel finalNodeModel = nodeModel;

        // bootstrap

        testTaskExecutor.execute(BootstrapMachine.class, new BootstrapMachineConfig() {
            @Override
            public String getBootstrapScriptResourcePath() {
                return bootstrapScriptResourcePath;
            }

            @Override
            public NodeModel getNodeModel() {
                return finalNodeModel;
            }
        }, softlayerPoolSettings, null);

        logger.info("after bootstrap machine, node status is [{}]", finalNodeModel.nodeStatus);
        Assert.isTrue(finalNodeModel.nodeStatus == NodeStatus.BOOTSTRAPPED,
                String.format("node status should be [%s]", NodeStatus.BOOTSTRAPPED));

        // delete

        testTaskExecutor.execute(DeleteMachine.class, new DeleteMachineConfig() {
            @Override
            public NodeModel getNodeModel() {
                return finalNodeModel;
            }
        }, softlayerPoolSettings, null);

        Assert.isNull(poolManagerApi.getNode(finalNodeModel.id), "node should not be found after deletion");
    }

    @Test
    public void testPoolStatus() {

/*
        // populating database with some mock nodes, for status collection
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('softlayer', 'CREATED', 'a')");
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('softlayer', 'CREATED', 'b')");
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('softlayer', 'BOOTSTRAPPED', 'c')");
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('hp', 'CREATED', '1')");
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('hp', 'BOOTSTRAPPED', '2')");
        jdbcTemplate.update("insert into nodes (pool_id,node_status,machine_id) values ('hp', 'OCCUPIED', '2')");
*/


        ManagerSettings managerSettings = settingsDataAccessManager.read();

        logger.info("looking for softlayer pool settings in manager settings [{}]", managerSettings);
        PoolSettings softlayerPoolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.softlayer);

        Assert.notNull(softlayerPoolSettings, "pool settings should not be null");

        logger.info("getting pool status list...");
        Collection<PoolStatus> poolStatusList = poolManagerApi.listStatuses();
        logger.info("got pool status list [{}]", poolStatusList);
        Assert.notNull(poolStatusList, "pool status list should not be null");

        logger.info("getting pool status...");
        PoolStatus poolStatus = poolManagerApi.getStatus(softlayerPoolSettings);
        Assert.notNull(poolStatus, "pool status should not be null");

        Assert.isTrue(poolStatus.getCurrentSize()>= softlayerPoolSettings.getMinNodes()&& poolStatus.getCurrentSize() <= softlayerPoolSettings.getMaxNodes(),
                String.format("current size [%s] must be greater than or equal to min size [%s] and less than or equal to max size [%s]",
                        poolStatus.getCurrentSize(), softlayerPoolSettings.getMinNodes(), softlayerPoolSettings.getMaxNodes()));

    }


    @Test
    public void testPoolCrud() {

        ManagerSettings managerSettings = settingsDataAccessManager.read();

        logger.info("looking for softlayer pool settings in manager settings [{}]", managerSettings);
        PoolSettings softlayerPoolSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.softlayer);

        int nodesSize = 3;
        List<NodeModel> nodes = new ArrayList<NodeModel>(nodesSize);

        logger.info("creating [{}] nodes for pool with provider [{}]...", nodesSize, softlayerPoolSettings.getProvider().getName());
        for (int i = 0; i < nodesSize; i++) {
            nodes.add(new NodeModel()
                    .setPoolId(softlayerPoolSettings.getUuid())
                    .setNodeStatus(NodeStatus.CREATED)
                    .setMachineId("test_machine_id"));
        }

        NodeModel firstNode = nodes.get(0);

        // create

        for (NodeModel node : nodes) {
            logger.info("adding node [{}] to pool...", node);
            boolean added = nodesDao.create(node);
            logger.info("added [{}]", added);
            Assert.isTrue(added, "add node should return true if it was successfully added in the pool");
            logger.info("node id [{}], initial id [{}]", node.id, NodeModel.INITIAL_ID);
            Assert.isTrue(node.id != NodeModel.INITIAL_ID, "node id should be updated after added to the pool.");
        }

        // read

        logger.info("reading node with id [{}]...", firstNode.id);
        NodeModel readNode = nodesDao.read(firstNode.id);
        logger.info("returned node [{}]", readNode);
        Assert.notNull(readNode, String.format("failed to read node with id [%s]", firstNode.id));

        logger.info("reading none existing node...");
        readNode = nodesDao.read(-2);
        logger.info("returned node [{}]", readNode);
        Assert.isNull(readNode,
                String.format("non existing node should be null when read from the pool, but instead returned [%s]", readNode));

        logger.info("listing nodes for pool with provider [{}]...", softlayerPoolSettings.getProvider().getName());
        List<NodeModel> softlayerNodeModels = nodesDao.readAllOfPool(softlayerPoolSettings.getUuid());
        logger.info("returned nodes [{}]", softlayerNodeModels);

        Assert.notNull(softlayerNodeModels, String.format("failed to read nodes of pool with id [%s]", softlayerPoolSettings.getUuid()));
        Assert.notEmpty(softlayerNodeModels, "nodes in softlayer pool should not be empty");
        Assert.isTrue(softlayerNodeModels.size() == nodesSize,
                String.format("amount of nodes in softlayer pool should be [%s], but instead is [%s]", nodesSize, softlayerNodeModels.size()));

        // update

        logger.info("updating first node status from [{}] to [{}]", firstNode.nodeStatus, NodeStatus.BOOTSTRAPPED);
        firstNode.setNodeStatus(NodeStatus.BOOTSTRAPPED);
        int affectedByUpdate = nodesDao.update(firstNode);
        logger.info("affectedByUpdate [{}]", affectedByUpdate);

        Assert.isTrue(affectedByUpdate == 1,
                String.format("exactly ONE node should be updated, but instead the amount affected by the update is [%s]", affectedByUpdate));

        logger.info("reading first node after update, using id [{}]...", firstNode.id);
        NodeModel node = nodesDao.read(firstNode.id);
        logger.info("returned node [{}]", node);

        Assert.notNull(node, "failed to read a single node");

        Assert.isTrue(node.nodeStatus == NodeStatus.BOOTSTRAPPED,
                String.format("node status should be updated to [%s], but is [%s]", NodeStatus.BOOTSTRAPPED.name(), node.nodeStatus));

        // delete

        logger.info("removing node with id [{}]...", node.id);
        nodesDao.delete(node.id);

        logger.info("reading node after remove, using id [{}]...", node.id);
        node = nodesDao.read(node.id);
        logger.info("returned node [{}]", node);

        Assert.isNull(node, String.format("node should be null after it is removed, but instead returned [%s]", node));
    }


    private void buildDatabase() {
        // going through all files under the 'sql' folder, and executing all of them.
        Iterator<File> sqlFileIterator = org.apache.commons.io.FileUtils.iterateFiles(
                FileUtils.getFileInClasspath("sql"), new String[]{"sql"}, false);
        while (sqlFileIterator.hasNext()) {
            File file = sqlFileIterator.next();
            List<String> statements = readSqlStatementsFromFile(file);
            for (String stmt : statements) {
                jdbcTemplate.update(stmt);
            }
        }
    }

    private String readSqlStatementFromFile(File file) {

        String script = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            LineNumberReader fileReader = new LineNumberReader(in);
            script = JdbcTestUtils.readScript(fileReader);
        } catch (IOException e) {
            logger.error("failed to read sql script from file", e);
        }
        return script;
    }

    private List<String> readSqlStatementsFromFile(File file) {
        String script = readSqlStatementFromFile(file);
        List<String> statements = new ArrayList<String>();
        JdbcTestUtils.splitSqlScript(script, ';', statements);
        return statements;
    }

}












