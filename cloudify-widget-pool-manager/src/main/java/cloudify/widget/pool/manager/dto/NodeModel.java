package cloudify.widget.pool.manager.dto;

import cloudify.widget.api.clouds.ISshDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 6:46 PM
 */
public class NodeModel {

    private static Logger logger = LoggerFactory.getLogger(NodeModel.class);

    public static final int INITIAL_ID = -1;

    public long id = INITIAL_ID;
    public String poolId;
    public NodeStatus nodeStatus;
    public String machineId;
    public ISshDetails machineSshDetails;

    public NodeModel setId(long id) {
        this.id = id;
        return this;
    }

    public NodeModel setPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    public NodeModel setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
        return this;
    }

    public NodeModel setMachineId(String machineId) {
        this.machineId = machineId;
        return this;
    }

    /**
     * This method can't be named setSshDetails, or JSON mapping will fail.
     *
     * @param sshDetails
     * @return
     */
    public NodeModel setMachineSshDetails(ISshDetails sshDetails) {
        this.machineSshDetails = sshDetails;
        return this;
    }


    @Override
    public String toString() {
        return "NodeModel{" +
                "id=" + id +
                ", poolId='" + poolId + '\'' +
                ", nodeStatus=" + nodeStatus +
                ", machineId='" + machineId + '\'' +
                ", machineSshDetails='" + machineSshDetails + '\'' +
                '}';
    }

}
