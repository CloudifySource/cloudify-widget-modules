package cloudify.widget.hpcloudcompute;

/**
 * User: eliranm
 * Date: 6/11/14
 * Time: 2:43 PM
 */
public class OpenstackFloatingIp {
    private String instanceId;
    private String ip;
    private String fixedIp;
    private String id;

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public void setFixedIp(String fixedIp) {
        this.fixedIp = fixedIp;
    }

    public String getFixedIp() {
        return fixedIp;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
