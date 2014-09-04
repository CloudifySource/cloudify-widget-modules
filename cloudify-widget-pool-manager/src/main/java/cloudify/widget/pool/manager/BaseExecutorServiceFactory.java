package cloudify.widget.pool.manager;

import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by sefi on 9/3/14.
 */
public abstract class BaseExecutorServiceFactory implements FactoryBean<ExecutorService> {

    protected int corePoolSize = 200;
    protected ExecutorService executorService;

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public Class<?> getObjectType() {
        return ExecutorService.class;
    }

}
