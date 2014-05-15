package cloudify.widget.ec2;


import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
public class Ec2CloudServerCreated implements CloudServerCreated {

    private final NodeMetadata nodeMetadata;

    public Ec2CloudServerCreated(NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
    }

    @Override
    public String getId() {
        return nodeMetadata.getId();
    }

    @Override
    public ISshDetails getSshDetails() {

        LoginCredentials loginCredentials = nodeMetadata.getCredentials();
        if (loginCredentials == null) {
            throw new RuntimeException("LoginCredentials is null");
        }
        String user = loginCredentials.getUser();
        String privateKey = loginCredentials.getPrivateKey();
        String publicIp = CollectionUtils.first(nodeMetadata.getPublicAddresses());
        int port = nodeMetadata.getLoginPort();

        return new Ec2SshDetails(port, user, privateKey, publicIp);
    }
}