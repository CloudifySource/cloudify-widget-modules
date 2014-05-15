package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.ServerIp;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.NodeMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * User: eliranm
 * Date: 2/4/14
 * Time: 4:20 PM
 */
public class SoftlayerCloudServer implements CloudServer {

    private final NodeMetadata computeMetadata;
    private final ComputeService computeService;

    private static Logger logger = LoggerFactory.getLogger(SoftlayerCloudServer.class);

    public SoftlayerCloudServer(ComputeService computeService, NodeMetadata computeMetadata) {
        this.computeService = computeService;
        this.computeMetadata = computeMetadata;
    }

    @Override
    public String getId() {
        return computeMetadata.getId();
    }

    @Override
    public String getName() {
        return computeMetadata.getName();
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
        NodeMetadata.Status status = null;
        if (computeMetadata != null) {
            status = computeMetadata.getStatus();
        }
        String statusStr = "";
        if (status != null) {
            statusStr = status.toString();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("extracted status from node metadata. status object is [{}], status string is [{}]", status, statusStr);
        }
        return SoftlayerCloudServerStatus.fromValue(statusStr);
    }

    @Override
    public ServerIp getServerIp() {
        ServerIp serverIp = new ServerIp();
        Set<String> publicAddresses = computeMetadata.getPublicAddresses();
        Set<String> privateAddresses = computeMetadata.getPrivateAddresses();
        if( !CollectionUtils.isEmpty( publicAddresses ) ){
            serverIp.publicIp = CollectionUtils.first( publicAddresses ) ;
        }
        if( !CollectionUtils.isEmpty( privateAddresses ) ){
            serverIp.privateIp = CollectionUtils.first( privateAddresses ) ;
        }
        return serverIp;
    }
}
