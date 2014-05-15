package cloudify.widget.hpcloudcompute;


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
public class HpCloudComputeCloudServerCreated implements CloudServerCreated {

	private final NodeMetadata nodeMetadata;

	public HpCloudComputeCloudServerCreated(NodeMetadata nodeMetadata){
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
        String privateKey = loginCredentials.getPrivateKey();
        int port = nodeMetadata.getLoginPort();
        String publicIp = CollectionUtils.first(nodeMetadata.getPublicAddresses());

        return new HpCloudComputeSshDetails( port, user, privateKey, publicIp );
    }

}