package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.IConnectDetails;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/11/14
 * Time: 10:31 AM
 */
public class SoftlayerConnectDetails implements IConnectDetails {
    public String username;
    public String key;

    /**
     * this should've been on the template builder, but it was simpler to add it on the context.
     */
    public String networkId;

    public SoftlayerConnectDetails() {}

    public String getUsername() {
        return username;
    }

    public SoftlayerConnectDetails setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getKey() {
        return key;
    }

    public SoftlayerConnectDetails setKey(String key) {
        this.key = key;
        return this;
    }

    public String getNetworkId() {
        return networkId;
    }

    public SoftlayerConnectDetails setNetworkId(String networkId) {
        this.networkId = networkId;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoftlayerConnectDetails that = (SoftlayerConnectDetails) o;

        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (networkId != null ? !networkId.equals(that.networkId) : that.networkId != null) return false;
        if (username != null ? !username.equals(that.username) : that.username != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (networkId != null ? networkId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SoftlayerConnectDetails{" +
                "username='" + username + '\'' +
                ", key='***'"+
                ", networkId='" + networkId + '\'' +
                '}';
    }
}
