package cloudify.widget.pool.manager.tasks;

import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.dto.PoolSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: eliranm
 * Date: 4/3/14
 * Time: 4:54 PM
 */
public abstract class AbstractPoolTask<T extends TaskConfig, V> implements  Task<T, V>{

    public T taskConfig;
    public PoolSettings poolSettings;
    @Autowired
    public CloudServerApiFactory cloudServerApiFactory;


    @Override
    public void setTaskConfig(T taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    public CloudServerApiFactory getCloudServerApiFactory() {
        return cloudServerApiFactory;
    }

    public void setCloudServerApiFactory(CloudServerApiFactory cloudServerApiFactory) {
        this.cloudServerApiFactory = cloudServerApiFactory;
    }
}
