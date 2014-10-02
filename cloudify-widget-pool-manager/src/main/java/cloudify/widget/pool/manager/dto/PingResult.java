package cloudify.widget.pool.manager.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by sefi on 9/11/14.
 */
public class PingResult {
    private PingStatus pingStatus;
    private List<PingResponse> pingResponses;
    private boolean aggregatedPingResponse;
    private long timestamp;

    public PingResult() {
        this.timestamp = new Date().getTime();
        setPingStatus(PingStatus.NOT_PINGED_YET);
        pingResponses = new ArrayList<PingResponse>();
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

    public List<PingResponse> getPingResponses() {
        return pingResponses;
    }

    public void setPingResponses(List<PingResponse> pingResponses) {
        this.pingResponses = pingResponses;
        boolean agg = true;

        for (PingResponse pingResponse : pingResponses) {
            if (!pingResponse.isWhiteListed()) {
                agg = false;
                break;
            }
        }

        aggregatedPingResponse = agg;
    }

    public boolean isAggregatedPingResponse() {
        return aggregatedPingResponse;
    }

    public void setAggregatedPingResponse(boolean aggregatedPingResponse) {
        this.aggregatedPingResponse = aggregatedPingResponse;
    }
}
