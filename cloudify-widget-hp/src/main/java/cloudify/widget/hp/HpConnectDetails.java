package cloudify.widget.hp;

import cloudify.widget.api.clouds.IConnectDetails;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/11/14
 * Time: 10:31 AM
 */
public class HpConnectDetails implements IConnectDetails {

    public static final String DEFAULT_API_VERSION = "1.1";

    private String project;
    private String key;
    private String secretKey;
    private String apiVersion = DEFAULT_API_VERSION;
    private String identityEndpoint;
    private String sshPrivateKey;
    private String region; // a - is US west, b - is US East


    public HpConnectDetails() {}

    public HpConnectDetails(String project, String key, String secretKey) {
        this( project, key, secretKey, DEFAULT_API_VERSION);
    }

    public HpConnectDetails(String project, String key, String secretKey, String apiVersion) {
        this.project = project;
        this.key = key;
        this.secretKey = secretKey;
        this.apiVersion = apiVersion;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getIdentityEndpoint() {
        return identityEndpoint;
    }

    public void setIdentityEndpoint(String identityEndpoint) {
        this.identityEndpoint = identityEndpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HpConnectDetails that = (HpConnectDetails) o;

        if (apiVersion != null ? !apiVersion.equals(that.apiVersion) : that.apiVersion != null) return false;
        if (identityEndpoint != null ? !identityEndpoint.equals(that.identityEndpoint) : that.identityEndpoint != null)
            return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (region != null ? !region.equals(that.region) : that.region != null) return false;
        if (secretKey != null ? !secretKey.equals(that.secretKey) : that.secretKey != null) return false;
        if (sshPrivateKey != null ? !sshPrivateKey.equals(that.sshPrivateKey) : that.sshPrivateKey != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        result = 31 * result + (identityEndpoint != null ? identityEndpoint.hashCode() : 0);
        result = 31 * result + (sshPrivateKey != null ? sshPrivateKey.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HpConnectDetails{" +
                "project='" + project + '\'' +
                ", key='" + key + '\'' +
                ", apiVersion='" + apiVersion + '\'' +
                ", identityEndpoint='" + identityEndpoint + '\'' +
                ", region='" + region + '\'' +
                '}';
    }

    public String getSshPrivateKey() {
        return sshPrivateKey;
    }

    public void setSshPrivateKey(String sshPrivateKey) {
        this.sshPrivateKey = sshPrivateKey;
    }
}