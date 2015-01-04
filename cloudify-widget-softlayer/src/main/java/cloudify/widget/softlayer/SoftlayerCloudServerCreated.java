package cloudify.widget.softlayer;


import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;
import org.json.JSONObject;

/**
 * Softlayer implementation of CloudServerCreated
 * @author evgenyf
 * Date: 10/7/13
 */
public class SoftlayerCloudServerCreated implements CloudServerCreated {

	private final JSONObject nodeMetadata;

	public SoftlayerCloudServerCreated(JSONObject nodeMetadata){
		this.nodeMetadata = nodeMetadata;
	}

    @Override
    public String getId() {
        return String.valueOf(nodeMetadata.getJSONObject("orderDetails").getJSONArray("virtualGuests").getJSONObject(0).getLong("id"));
    }

    @Override
    public ISshDetails getSshDetails() {
        JSONObject sshDetails = nodeMetadata.getJSONObject("sshDetails");
        if(sshDetails == null){
            throw new RuntimeException( "LoginCredentials is null" );
        }
        String user = sshDetails.getString("username");
        String password = sshDetails.getString("password");
        int port = sshDetails.getInt("port");
        String publicIp = sshDetails.getString("primaryIP");

        return new SoftlayerSshDetails( port, user, password, publicIp );
    }
}