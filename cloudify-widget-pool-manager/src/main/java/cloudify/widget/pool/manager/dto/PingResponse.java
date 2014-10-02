package cloudify.widget.pool.manager.dto;

/**
 * Created by sefi on 01/10/14.
 */
public class PingResponse {

    private int responseCode;
    private String errorMessage;
    private boolean isWhiteListed = false;
    private PingSettings pingSettings;

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isWhiteListed() {
        return isWhiteListed;
    }

    public void setWhiteListed(boolean isWhiteListed) {
        this.isWhiteListed = isWhiteListed;
    }

    public PingSettings getPingSettings() {
        return pingSettings;
    }

    public void setPingSettings(PingSettings pingSettings) {
        this.pingSettings = pingSettings;
    }
}
