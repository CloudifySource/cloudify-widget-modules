package cloudify.widget.pool.manager.dto;

import cloudify.widget.api.clouds.ISshDetails;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 6:46 PM
 */
public class NodeModel {

    public static final int INITIAL_ID = -1;

    public long id = INITIAL_ID;
    public String poolId;
    public NodeStatus nodeStatus;
    public String randomValue;
    public String machineId;
    public ISshDetails machineSshDetails;
    public PingResult pingStatus;
    public long expires;

    public NodeModel setPingStatus(PingResult pingStatus) {
        this.pingStatus = pingStatus;
        return this;
    }

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

    public String getRandomValue() {
        return randomValue;
    }

    public NodeModel setRandomValue(String randomValue) {
        this.randomValue = randomValue;
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

    public NodeModel setExpires(long expires) {
        this.expires = expires;
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
                ", pingResult='" + pingStatus + '\'' +
                ", expires='" + expires + '\'' +
                '}';
    }

}
