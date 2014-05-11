package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.node_management.BaseNodeManagementModule;
import cloudify.widget.pool.manager.node_management.Constraints;
import cloudify.widget.pool.manager.node_management.NodeManagementModuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * User: eliranm
 * Date: 4/27/14
 * Time: 2:30 PM
 */
public class NodeManagementExecutor {

    private static Logger logger = LoggerFactory.getLogger(NodeManagementExecutor.class);

    private ScheduledExecutorService executorService;

    private int terminationTimeoutInSeconds = 30;

    private int decisionExecutionIntervalInSeconds = 60;

    @Autowired
    private NodeManagementModuleProvider nodeManagementModuleProvider;

    @Autowired
    private ErrorsDao errorsDao;

    // TODO get rid of this - it has to be persisted somewhere
    // key: poolId, value: pool execution including scheduled futures returned from starting scheduled tasks for this pool's active modules, and the runner instance
    private Map<String, List<PoolExecution>> poolExecutions = new HashMap<String, List<PoolExecution>>();


    public void init() {

    }

    public void destroy() {
        executorService.shutdown();
        try {
            // Wait until all threads are finish
            executorService.awaitTermination(terminationTimeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("await termination interrupted", e);
        }
    }


    public void start(PoolSettings poolSettings) {
        List<NodeManagementModuleType> activeModules = poolSettings.getNodeManagement().getActiveModules();
        if (activeModules == null || activeModules.isEmpty()) {
            return;
        }
        LinkedList<PoolExecution> executionList = new LinkedList<PoolExecution>();
        for (NodeManagementModuleType activeModule : activeModules) {
            logger.info("starting scheduled execution of node management module [{}]", activeModule);
            PoolNodeManagementModuleRunner runner = new PoolNodeManagementModuleRunner(activeModule, poolSettings);
            ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(
                    runner, 0, decisionExecutionIntervalInSeconds, TimeUnit.SECONDS);
            executionList.add(new PoolExecution().setScheduled(scheduledFuture).setRunner(runner));
        }
        poolExecutions.put(poolSettings.getUuid(), executionList);
    }

    public void stop(PoolSettings poolSettings) {
        String poolSettingsUuid = poolSettings.getUuid();
        if (!poolExecutions.containsKey(poolSettingsUuid)) {
            return;
        }
        List<PoolExecution> executionList = poolExecutions.get(poolSettingsUuid);
        if (executionList != null && !executionList.isEmpty()) {
            logger.info("stopping all scheduled executions of node management modules for pool settings [{}]", poolSettingsUuid);
            for (PoolExecution execution : executionList) {
                execution.getScheduled().cancel(true);
            }
        }
        poolExecutions.remove(poolSettingsUuid);
    }

    public void update(PoolSettings poolSettings) {
        String poolSettingsUuid = poolSettings.getUuid();
        if (!poolExecutions.containsKey(poolSettingsUuid)) {
            return;
        }
        List<PoolExecution> executionList = poolExecutions.get(poolSettingsUuid);
        if (executionList != null && !executionList.isEmpty()) {
            logger.info("updating pool settings in all scheduled executions of node management modules for pool settings [{}]", poolSettingsUuid);
            for (PoolExecution execution : executionList) {
                execution.getRunner().updatePoolSettings(poolSettings);
            }
        }
    }


    public static class PoolExecution {

        // holds the execution id, to cancel it when pool settings is removed
        private ScheduledFuture scheduled;
        // used for updating the pool settings while the execution is running
        private PoolNodeManagementModuleRunner runner;

        public PoolNodeManagementModuleRunner getRunner() {
            return runner;
        }
        public PoolExecution setRunner(PoolNodeManagementModuleRunner runner) {
            this.runner = runner;
            return this;
        }
        public ScheduledFuture getScheduled() {
            return scheduled;
        }
        public PoolExecution setScheduled(ScheduledFuture scheduled) {
            this.scheduled = scheduled;
            return this;
        }
    }

    public class PoolNodeManagementModuleRunner implements Runnable {

        private Logger logger = LoggerFactory.getLogger(PoolNodeManagementModuleRunner.class);

        private BaseNodeManagementModule _nodeManagementModule;

        public PoolNodeManagementModuleRunner(NodeManagementModuleType type, PoolSettings poolSettings) {
            _nodeManagementModule = nodeManagementModuleProvider.fromType(type)
                    .having(new Constraints(poolSettings));
        }

        public void updatePoolSettings(PoolSettings poolSettings) {
            _nodeManagementModule.having(new Constraints(poolSettings));
        }

        @Override
        public void run() {
            logger.debug("running node management module [{}]", _nodeManagementModule.getClass());
            _nodeManagementModule
                    .decide()
                    .execute();

        }
    }

    public void setDecisionExecutionIntervalInSeconds(int decisionExecutionIntervalInSeconds) {
        this.decisionExecutionIntervalInSeconds = decisionExecutionIntervalInSeconds;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }
}
