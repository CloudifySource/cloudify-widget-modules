package cloudify.widget.ec2;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.ServerIp;
import cloudify.widget.common.CollectionUtils;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.NodeMetadata;

import java.util.Set;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
public class Ec2CloudServer implements CloudServer {

    private final NodeMetadata computeMetadata;
    private final ComputeService computeService;

    public Ec2CloudServer(ComputeService computeService, NodeMetadata computeMetadata) {
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
        return getStatus() == Ec2CloudServerStatus.RUNNING;
    }

    @Override
    public boolean isStopped(){
        return !isRunning();
    }

    Ec2CloudServerStatus getStatus() {
        NodeMetadata.Status status = computeService.getNodeMetadata(computeMetadata.getId()).getStatus();
        return Ec2CloudServerStatus.fromValue(status.toString());
    }

    @Override
    public ServerIp getServerIp() {
        ServerIp serverIp = new ServerIp();
        Set<String> publicAddresses = computeMetadata.getPublicAddresses();
        Set<String> privateAddresses = computeMetadata.getPrivateAddresses();
        if( !CollectionUtils.isEmpty(publicAddresses) ){
            serverIp.publicIp = CollectionUtils.first( publicAddresses ) ;
        }
        if( !CollectionUtils.isEmpty( privateAddresses ) ){
            serverIp.privateIp = CollectionUtils.first( privateAddresses ) ;
        }
        return serverIp;
    }
}