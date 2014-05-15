package cloudify.widget.pool.manager.tasks;

import com.google.common.util.concurrent.FutureCallback;

/**
 * A tag interface, only to serve as an abstraction layer on top of guava's future callbacks
 *
 * @param <R> Result type
 */
public interface TaskCallback<R> extends FutureCallback<R> {
}
