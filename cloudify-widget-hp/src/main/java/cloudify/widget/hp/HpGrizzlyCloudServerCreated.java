package cloudify.widget.hp;


import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.api.clouds.ISshDetails;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
public class HpGrizzlyCloudServerCreated implements CloudServerCreated {

	private final String id;
    private final HpGrizzlySshDetails sshDetails;

    public HpGrizzlyCloudServerCreated(String id, HpGrizzlySshDetails sshDetails){
		this.id = id;
        this.sshDetails = sshDetails;
	}

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISshDetails getSshDetails() {
        return sshDetails;
    }

}