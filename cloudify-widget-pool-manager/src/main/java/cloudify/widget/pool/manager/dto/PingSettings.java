package cloudify.widget.pool.manager.dto;

import java.util.List;

/**
 * Created by sefi on 9/9/14.
 */
public class PingSettings {

    private String url;
    private List<String> whiteList;
    private int retryCount  = 5;
    private int pingTimeout = 5000;

    /**
     * The url to be pinged for the given IP. The hostname should be '$HOST' so it can be replaced with specific IP.
     * It should be in the format: http://$HOST:8080.
     * Both http & https are supported
     */
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * List of legal status codes that represent a successful ping
     */
    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }

    /**
     * Number of times to retry each ping before it is considered down
     */
    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * Ping timeout. If ping exceeds this timeout, it fails.
     */
    public int getPingTimeout() {
        return pingTimeout;
    }

    public void setPingTimeout(int pingTimeout) {
        this.pingTimeout = pingTimeout;
    }
}
