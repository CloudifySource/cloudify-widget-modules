package cloudify.widget.pool.manager.tasks;

import cloudify.widget.pool.manager.dto.PoolSettings;

/**
 * User: eliranm
 * Date: 4/3/14
 * Time: 4:54 PM
 */
public abstract class AbstractPoolTask<T extends TaskConfig, V> implements  Task<T, V>{

    public T taskConfig;
    public PoolSettings poolSettings;


    @Override
    public void setTaskConfig(T taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }
}
