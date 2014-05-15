package cloudify.widget.pool.manager.tasks;

/**
 * a callback that does nothing, to safely pass to the executor in case a null callback was found.
 */
public class NoopTaskCallback implements TaskCallback {
    @Override
    public void onSuccess(Object result) {}
    @Override
    public void onFailure(Throwable t) {}
}
