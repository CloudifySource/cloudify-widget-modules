package cloudify.widget.hp;

/**
 * User: eliranm
 * Date: 6/11/14
 * Time: 2:29 PM
 */
public class OpenstackNode {
    private String id;
    private String status;
    private String name;
    private String privateIp;
    private String publicIp;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPrivateIp(String privateIp) {
        this.privateIp = privateIp;
    }

    public String getPrivateIp() {
        return privateIp;
    }

    public void setPublicIp(String publicIp) {
        this.publicIp = publicIp;
    }

    public String getPublicIp() {
        return publicIp;
    }
}
