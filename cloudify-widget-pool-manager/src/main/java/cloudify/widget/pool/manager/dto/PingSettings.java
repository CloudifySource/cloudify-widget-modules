package cloudify.widget.pool.manager.dto;

import java.util.List;

/**
 * Created by sefi on 9/9/14.
 */
public class PingSettings {

    private String port;                                    // The port to be pinged for the given IP
    private List<String> statusCodesWhiteList;             // List of legal status codes that represent a successful ping
    private int retryCount  = 5;                            // Number of times to retry each ping before it is considered down

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public List<String> getStatusCodesWhiteList() {
        return statusCodesWhiteList;
    }

    public void setStatusCodesWhiteList(List<String> statusCodesWhiteList) {
        this.statusCodesWhiteList = statusCodesWhiteList;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
}
