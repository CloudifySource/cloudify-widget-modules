package cloudify.widget.ec2;

import cloudify.widget.api.clouds.IConnectDetails;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/11/14
 * Time: 10:31 AM
 */
public class Ec2ConnectDetails implements IConnectDetails {

    private String accessId;
    private String secretAccessKey;

    public Ec2ConnectDetails() {}

    public Ec2ConnectDetails(String accessId, String secretAccessKey) {
        this.accessId = accessId;
        this.secretAccessKey = secretAccessKey;
    }

    public String getAccessId() {
        return accessId;
    }

    public void setAccessId(String accessId) {
        this.accessId = accessId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = secretAccessKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ec2ConnectDetails that = (Ec2ConnectDetails) o;

        if (accessId != null ? !accessId.equals(that.accessId) : that.accessId != null) return false;
        if (secretAccessKey != null ? !secretAccessKey.equals(that.secretAccessKey) : that.secretAccessKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = accessId != null ? accessId.hashCode() : 0;
        result = 31 * result + (secretAccessKey != null ? secretAccessKey.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Ec2ConnectDetails{" +
                "accessId='" + accessId + '\'' +
                ", secretAccessKey='***'" +
                '}';
    }
}
