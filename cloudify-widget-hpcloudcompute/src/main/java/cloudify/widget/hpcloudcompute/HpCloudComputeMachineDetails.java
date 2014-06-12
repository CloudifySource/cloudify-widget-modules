package cloudify.widget.hpcloudcompute;

/**
 * User: eliranm
 * Date: 6/11/14
 * Time: 2:36 PM
 */
public class HpCloudComputeMachineDetails {
    private String publicAddress;
    private String machineId;
    private boolean agentRunning;
    private boolean cloudifyInstalled;
    private String remoteUsername;
    private String privateAddress;

    public void setPublicAddress(String publicAddress) {
        this.publicAddress = publicAddress;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setAgentRunning(boolean agentRunning) {
        this.agentRunning = agentRunning;
    }

    public boolean isAgentRunning() {
        return agentRunning;
    }

    public void setCloudifyInstalled(boolean cloudifyInstalled) {
        this.cloudifyInstalled = cloudifyInstalled;
    }

    public boolean isCloudifyInstalled() {
        return cloudifyInstalled;
    }

    public void setRemoteUsername(String remoteUsername) {
        this.remoteUsername = remoteUsername;
    }

    public String getRemoteUsername() {
        return remoteUsername;
    }

    public void setPrivateAddress(String privateAddress) {
        this.privateAddress = privateAddress;
    }

    public String getPrivateAddress() {
        return privateAddress;
    }
}
