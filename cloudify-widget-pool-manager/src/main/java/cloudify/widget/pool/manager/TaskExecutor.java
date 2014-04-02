package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.tasks.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 5:35 PM
 */
public class TaskExecutor {

    private static Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

    private ListeningExecutorService executorService;

//    private ListeningExecutorService backgroundExecutorService;

    private int terminationTimeoutInSeconds = 30;

    @Autowired
    private ITasksDao tasksDao;


    public void init() {
    }

    public void destroy() {
        executorService.shutdown();
//        backgroundExecutorService.shutdown();
        try {
            // Wait until all threads are finish
            executorService.awaitTermination(terminationTimeoutInSeconds, TimeUnit.SECONDS);
//            backgroundExecutorService.awaitTermination(terminationTimeoutInSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            logger.error("await termination interrupted", e);
        }
    }

    public <T extends Task> void execute(T task, TaskConfig taskConfig, PoolSettings poolSettings) {
        execute(task, taskConfig, poolSettings, new NoopTaskCallback());
    }

    public <T extends Task, C extends TaskConfig, R> void execute(T task, C taskConfig, PoolSettings poolSettings, TaskCallback<R> taskCallback) {
        assert executorService != null : "executor must not be null";
        assert poolSettings != null : "pool settings must not be null";

        TaskRegistrar.TaskDecorator worker = new TaskRegistrar.TaskDecoratorImpl<C, R>(task);
        worker.setTasksDao(tasksDao);
        worker.setPoolSettings(poolSettings);
        worker.setTaskConfig(taskConfig);

        if (worker != null) {
            ListenableFuture listenableFuture = executorService.submit(worker);
            if (taskCallback == null) {
                taskCallback = new NoopTaskCallback();
            }
            Futures.addCallback(listenableFuture, taskCallback);
        }
    }


    public void setTerminationTimeoutInSeconds(int terminationTimeoutInSeconds) {
        this.terminationTimeoutInSeconds = terminationTimeoutInSeconds;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = MoreExecutors.listeningDecorator(executorService);
    }

/*
    public void setBackgroundExecutorService(ListeningExecutorService backgroundExecutorService) {
        this.backgroundExecutorService = backgroundExecutorService;
    }
*/
}
