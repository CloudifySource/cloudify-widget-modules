package cloudify.widget.pool.manager;

import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 5:12 PM
 */
public class FixedSizeExecutorServiceFactory implements FactoryBean<ExecutorService> {

    private int poolSize = 200;

    @Override
    public ExecutorService getObject() throws Exception {
        return Executors.newFixedThreadPool(poolSize);
    }

    @Override
    public Class<?> getObjectType() {
        return ExecutorService.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
