package cloudify.widget.pool.manager;

import org.springframework.beans.factory.FactoryBean;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 5:12 PM
 */
public class FixedSizeExecutorServiceFactory extends BaseExecutorServiceFactory {

    @Override
    public ExecutorService getObject() throws Exception {
        executorService = Executors.newFixedThreadPool(corePoolSize);
        return executorService;
    }

}
