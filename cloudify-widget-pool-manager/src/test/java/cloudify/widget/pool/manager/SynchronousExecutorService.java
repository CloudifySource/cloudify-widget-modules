package cloudify.widget.pool.manager;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 5:23 PM
 */
public class SynchronousExecutorService extends ThreadPoolExecutor {

    private boolean shutdown;

    public SynchronousExecutorService(int poolSize) {
        super(poolSize, poolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
    }

    @Override
    public void shutdown() {
        shutdown = true;
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown = true;
        return Collections.emptyList();
    }

    @Override
    public boolean isShutdown() {
        shutdown = true;
        return shutdown;
    }

    @Override
    public boolean isTerminated() {
        return shutdown;
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) {
        return true;
    }

    @Override
    public void execute(final Runnable command) {
        command.run();
    }
}
