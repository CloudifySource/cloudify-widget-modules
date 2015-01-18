package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.ServerIp;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * User: eliranm
 * Date: 2/4/14
 * Time: 4:20 PM
 */
public class SoftlayerCloudServer implements CloudServer {

    private JSONObject metadata;

    private static Logger logger = LoggerFactory.getLogger(SoftlayerCloudServer.class);

    public SoftlayerCloudServer(JSONObject metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getId() {
        return String.valueOf(metadata.getLong("id"));
    }

    @Override
    public String getName() {
        return metadata.getString("hostname");
    }

    @Override
    public boolean isRunning(){
        return getStatus() == SoftlayerCloudServerStatus.RUNNING;
    }

    @Override
    public boolean isStopped(){
        return getStatus() == SoftlayerCloudServerStatus.STOPPED || getStatus() == SoftlayerCloudServerStatus.UNRECOGNIZED;
    }

    public SoftlayerCloudServerStatus getStatus() {
//        NodeMetadata.Status status = null;
//        if (computeMetadata != null) {
//            status = computeMetadata.getStatus();
//        }
//        String statusStr = "";
//        if (status != null) {
//            statusStr = status.toString();
//        }
//        if (logger.isDebugEnabled()) {
//            logger.debug("extracted status from node metadata. status object is [{}], status string is [{}]", status, statusStr);
//        }
//        return SoftlayerCloudServerStatus.fromValue(statusStr);

        // return RUNNING - due to the move to REST API.
        return SoftlayerCloudServerStatus.RUNNING;
    }

    @Override
    public ServerIp getServerIp() {
        ServerIp serverIp = new ServerIp();

        if (metadata.has("primaryIpAddress")) {
            serverIp.publicIp = metadata.getString("primaryIpAddress");
        }

        if (metadata.has("primaryBackendIpAddress")) {
            serverIp.privateIp = metadata.getString("primaryBackendIpAddress");
        }

        return serverIp;
    }
}
