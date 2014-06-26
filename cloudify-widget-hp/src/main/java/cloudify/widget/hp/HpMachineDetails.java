package cloudify.widget.hp;

/**
 * User: eliranm
 * Date: 6/11/14
 * Time: 2:36 PM
 */
public class HpMachineDetails {
    private String publicAddress;
    private String machineId;
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
