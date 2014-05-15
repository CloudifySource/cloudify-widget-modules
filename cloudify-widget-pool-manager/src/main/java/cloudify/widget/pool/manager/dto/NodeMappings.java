package cloudify.widget.pool.manager.dto;

import cloudify.widget.api.clouds.ServerIp;

/**
 * User: eliranm
 * Date: 3/24/14
 * Time: 1:45 PM
 */
public class NodeMappings {

    private long nodeId = -1; // if not in DB it is -1, otherwise the DB id
    private String machineId; // from cloud
    private ServerIp ip; // the ip

    public long getNodeId() {
        return nodeId;
    }

    public NodeMappings setNodeId(long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getMachineId() {
        return machineId;
    }

    public NodeMappings setMachineId(String machineId) {
        this.machineId = machineId;
        return this;
    }

    public ServerIp getIp() {
        return ip;
    }

    public NodeMappings setIp(ServerIp ip) {
        this.ip = ip;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NodeMappings that = (NodeMappings) o;

        if (nodeId != that.nodeId) return false;
        if (ip != null ? !ip.equals(that.ip) : that.ip != null) return false;
        if (machineId != null ? !machineId.equals(that.machineId) : that.machineId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (nodeId ^ (nodeId >>> 32));
        result = 31 * result + (machineId != null ? machineId.hashCode() : 0);
        result = 31 * result + (ip != null ? ip.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NodeMappings{" +
                "nodeId=" + nodeId +
                ", machineId='" + machineId + '\'' +
                ", ip=" + ip +
                '}';
    }
}
