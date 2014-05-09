package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.node_management.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
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
    // key: poolId, value: scheduled future returned from starting the scheduled task for this pool
    private Map<String, ScheduledFuture> poolExecutions = new HashMap<String, ScheduledFuture>();


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
        ScheduledFuture<?> scheduledFuture = executorService.scheduleAtFixedRate(
                new PoolNodeManagementRunner(poolSettings), 0, decisionExecutionIntervalInSeconds, TimeUnit.SECONDS);
        poolExecutions.put(poolSettings.getUuid(), scheduledFuture);
    }

    public void stop(PoolSettings poolSettings) {
        ScheduledFuture scheduledFuture = poolExecutions.get(poolSettings.getUuid());
        if (scheduledFuture == null) {
            return;
        }
        scheduledFuture.cancel(true);
    }


    public class PoolNodeManagementRunner implements Runnable {

        private Logger logger = LoggerFactory.getLogger(PoolNodeManagementRunner.class);

        private final PoolSettings _poolSettings;

        public PoolNodeManagementRunner(PoolSettings poolSettings) {
            _poolSettings = poolSettings;
        }

        @Override
        public void run() {
            logger.debug("running node management for pool [{}]", _poolSettings.getUuid());

            // TODO create separate process for each module to prevent blocking of all modules
            List<NodeManagementModuleType> activeModules = _poolSettings.getNodeManagement().getActiveModules();
            for (NodeManagementModuleType activeModule : activeModules) {
                BaseNodeManagementModule nodeManagementModule = nodeManagementModuleProvider.fromType(activeModule);
                logger.debug("running node management module [{}]", nodeManagementModule.getClass());
                nodeManagementModule
                        .having(new Constraints(_poolSettings))
                        .decide()
                        .execute();
            }

        }
    }

    public void setDecisionExecutionIntervalInSeconds(int decisionExecutionIntervalInSeconds) {
        this.decisionExecutionIntervalInSeconds = decisionExecutionIntervalInSeconds;
    }

    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }
}
