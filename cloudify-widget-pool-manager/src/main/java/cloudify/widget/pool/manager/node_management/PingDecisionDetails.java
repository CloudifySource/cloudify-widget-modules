package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.PingResult;

/**
 * Created by sefi on 9/11/14.
 */
public class PingDecisionDetails extends NodeIdProvidingDecisionDetails<PingDecisionDetails> {
    private PingResult pingResult;

    public PingResult getPingResult() {
        return pingResult;
    }

    public PingDecisionDetails setPingResult(PingResult pingResult) {
        this.pingResult = pingResult;
        return this;
    }

}
