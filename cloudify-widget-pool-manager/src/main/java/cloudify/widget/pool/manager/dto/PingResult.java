package cloudify.widget.pool.manager.dto;

import java.util.Date;

/**
 * Created by sefi on 9/11/14.
 */
public class PingResult {
    private PingStatus pingStatus;
    private long timestamp;

    public PingResult() {
        this.timestamp = new Date().getTime();
        setPingStatus(PingStatus.NOT_PINGED_YET);
    }

    public PingStatus getPingStatus() {
        return pingStatus;
    }

    public void setPingStatus(PingStatus pingStatus) {
        this.pingStatus = pingStatus;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
