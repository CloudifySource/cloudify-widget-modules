package cloudify.widget.hpcloudcompute;


import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.api.clouds.ISshDetails;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
public class HpCloudComputeOpenstackCloudServerCreated implements CloudServerCreated {

	private final String id;

	public HpCloudComputeOpenstackCloudServerCreated(String id){
		this.id = id;
	}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISshDetails getSshDetails() {

        // TODO return ssh details
/*
        LoginCredentials loginCredentials = nodeMetadata.getCredentials();
        if(loginCredentials == null){
            throw new RuntimeException( "LoginCredentials is null" );
        }
        String user = loginCredentials.getUser();
        String privateKey = loginCredentials.getPrivateKey();
        int port = nodeMetadata.getLoginPort();
        String publicIp = CollectionUtils.first(nodeMetadata.getPublicAddresses());

        return new HpCloudComputeSshDetails( port, user, privateKey, publicIp );
*/
        return new HpCloudComputeSshDetails();
    }

}