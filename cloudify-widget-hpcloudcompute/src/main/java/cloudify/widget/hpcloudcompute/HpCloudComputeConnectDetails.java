package cloudify.widget.hpcloudcompute;

import cloudify.widget.api.clouds.IConnectDetails;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/11/14
 * Time: 10:31 AM
 */
public class HpCloudComputeConnectDetails implements IConnectDetails {

    public static final String DEFAULT_API_VERSION = "1.1";

    private String project;
    private String key;
    private String secretKey;
    private String apiVersion = DEFAULT_API_VERSION;

    public HpCloudComputeConnectDetails() {}

    public HpCloudComputeConnectDetails( String project, String key, String secretKey ) {
        this( project, key, secretKey, DEFAULT_API_VERSION);
    }

    public HpCloudComputeConnectDetails( String project, String key, String secretKey, String apiVersion ) {
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

    public void setKeyFile(String secretFile) {
        this.secretKey = secretKey;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HpCloudComputeConnectDetails that = (HpCloudComputeConnectDetails) o;

        if (apiVersion != null ? !apiVersion.equals(that.apiVersion) : that.apiVersion != null) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (project != null ? !project.equals(that.project) : that.project != null) return false;
        if (secretKey != null ? !secretKey.equals(that.secretKey) : that.secretKey != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = project != null ? project.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (secretKey != null ? secretKey.hashCode() : 0);
        result = 31 * result + (apiVersion != null ? apiVersion.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HpCloudComputeConnectDetails{" +
                "project='" + project + '\'' +
                ", key='" + key + '\'' +
                ", secretKey='***'" +
                ", apiVersion='" + apiVersion + '\'' +
                '}';
    }
}