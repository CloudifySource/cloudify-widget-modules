package cloudify.widget.pool.manager.dto;

import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.ec2.Ec2SshDetails;
import cloudify.widget.hpcloudcompute.HpCloudComputeSshDetails;
import cloudify.widget.softlayer.SoftlayerSshDetails;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * User: eliranm
 * Date: 4/3/14
 * Time: 6:20 PM
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name",
        visible = true) // we want the 'name' property to be in the output as well
@JsonSubTypes({
        @JsonSubTypes.Type(value = NodeModelSshDetails.HpCloudComputeNodeModelSshDetails.class, name = "hp"),
        @JsonSubTypes.Type(value = NodeModelSshDetails.Ec2NodeModelSshDetails.class, name = "ec2"),
        @JsonSubTypes.Type(value = NodeModelSshDetails.SoftlayerNodeModelSshDetails.class, name = "softlayer")})

public abstract class NodeModelSshDetails {

    public abstract ISshDetails getMachineSshDetails();

    public static ISshDetails toSshDetails(NodeModelSshDetails nodeModelSshDetails) {
        return nodeModelSshDetails.getMachineSshDetails();
    }

    public static NodeModelSshDetails fromSshDetails(ISshDetails sshDetails) {
        if (sshDetails instanceof Ec2SshDetails) {
            Ec2SshDetails details = (Ec2SshDetails) sshDetails;
            Ec2NodeModelSshDetails result = new Ec2NodeModelSshDetails();
            result.machineSshDetails = details;
            return result;
        }

        if (sshDetails instanceof HpCloudComputeSshDetails) {
            HpCloudComputeSshDetails details = (HpCloudComputeSshDetails) sshDetails;
            HpCloudComputeNodeModelSshDetails result = new HpCloudComputeNodeModelSshDetails();
            result.machineSshDetails = details;
            return result;
        }

        if (sshDetails instanceof SoftlayerSshDetails) {
            SoftlayerSshDetails details = (SoftlayerSshDetails) sshDetails;
            SoftlayerNodeModelSshDetails result = new SoftlayerNodeModelSshDetails();
            result.machineSshDetails = details;
            return result;
        }

        throw new RuntimeException("unsupported ssh details type " + sshDetails.getClass());
    }

    public static class SoftlayerNodeModelSshDetails extends NodeModelSshDetails {
        public String name = "softlayer";
        public SoftlayerSshDetails machineSshDetails;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public SoftlayerSshDetails getMachineSshDetails() {
            return machineSshDetails;
        }

        public void setMachineSshDetails(SoftlayerSshDetails machineSshDetails) {
            this.machineSshDetails = machineSshDetails;
        }
    }

    public static class HpCloudComputeNodeModelSshDetails extends NodeModelSshDetails {
        public String name = "hp";
        public HpCloudComputeSshDetails machineSshDetails;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public HpCloudComputeSshDetails getMachineSshDetails() {
            return machineSshDetails;
        }

        public void setMachineSshDetails(HpCloudComputeSshDetails machineSshDetails) {
            this.machineSshDetails = machineSshDetails;
        }
    }

    public static class Ec2NodeModelSshDetails extends NodeModelSshDetails {
        public String name = "ec2";
        public Ec2SshDetails machineSshDetails;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Ec2SshDetails getMachineSshDetails() {
            return machineSshDetails;
        }

        public void setMachineSshDetails(Ec2SshDetails machineSshDetails) {
            this.machineSshDetails = machineSshDetails;
        }
    }
}
