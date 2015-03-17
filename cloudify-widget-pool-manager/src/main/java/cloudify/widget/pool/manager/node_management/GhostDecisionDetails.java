package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.NodeMappings;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * Holds details
 *
 * Created by guym on 3/17/15.
 */
public class GhostDecisionDetails implements DecisionDetails {

    @JsonIgnore
    // time of first detection of ghost
    public long since = System.currentTimeMillis();

    @JsonIgnore
    // data of cloud machine. ghosts do not have DB model that maps to it
    private NodeMappings nodeMappings;

    @JsonIgnore
    private boolean calledGhostbusters = false;

    public String machineId = null;

    @JsonIgnore
    private long timeLimit = 0; // always

    public GhostDecisionDetails() {
    }

    public GhostDecisionDetails(NodeMappings nodeMappings, long timeLimit ) {
        this.nodeMappings = nodeMappings;
        this.timeLimit = timeLimit;
        this.machineId = nodeMappings.getMachineId();
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public NodeMappings getNodeMappings() {
        return nodeMappings;
    }

    // is this ghost alive for too long?
    public boolean isHaunting( ){
        return System.currentTimeMillis() - since > timeLimit;
    }

    public boolean isCalledGhostbusters() {
        return calledGhostbusters;
    }

    public void setCalledGhostbusters(boolean calledGhostbusters) {
        this.calledGhostbusters = calledGhostbusters;
    }

    @Override
    public String toString() {
        return "GhostDecisionDetails{" +
                "since=" + since +
                ", nodeMappings=" + nodeMappings +
                ", timeLimit=" + timeLimit +
                '}';
    }
}
