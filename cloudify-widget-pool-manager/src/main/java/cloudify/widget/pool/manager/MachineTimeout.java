package cloudify.widget.pool.manager;

import java.util.concurrent.TimeUnit;

/**
 * User: eliranm
 * Date: 5/10/14
 * Time: 1:29 AM
 */
public class MachineTimeout {

    private long duration;

    private TimeUnit timeUnit;

    public long getDuration() {
        return duration;
    }

    public MachineTimeout setDuration(long duration) {
        this.duration = duration;
        return this;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public MachineTimeout setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public long inMillis() {
        return timeUnit.toMillis(duration);
    }
}
