package cloudify.widget.pool.manager.tasks;

import cloudify.widget.pool.manager.dto.PoolSettings;

import java.util.concurrent.Callable;

/**
 * @param <C> The task config implementation.
 * @param <R> The expected task result data.
 */
public interface Task<C extends TaskConfig, R> extends Callable<R> {

    TaskName getTaskName();

    void setPoolSettings(PoolSettings poolSettings);

    void setTaskConfig(C taskConfig);
}
