package cloudify.widget.softlayer;


import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;

/**
 * Softlayer implementation of CloudServerCreated
 * @author evgenyf
 * Date: 10/7/13
 */
public class SoftlayerCloudServerCreated implements CloudServerCreated {

	private final NodeMetadata nodeMetadata;

	public SoftlayerCloudServerCreated(NodeMetadata nodeMetadata){
		this.nodeMetadata = nodeMetadata;
	}

    @Override
    public String getId() {
        return nodeMetadata.getId();
    }

    @Override
    public ISshDetails getSshDetails() {
        LoginCredentials loginCredentials = nodeMetadata.getCredentials();
        if(loginCredentials == null){
            throw new RuntimeException( "LoginCredentials is null" );
        }
        String user = loginCredentials.getUser();
        String password = loginCredentials.getPassword();
        int port = nodeMetadata.getLoginPort();
        String publicIp = CollectionUtils.first(nodeMetadata.getPublicAddresses());

        return new SoftlayerSshDetails( port, user, password, publicIp );
    }
}