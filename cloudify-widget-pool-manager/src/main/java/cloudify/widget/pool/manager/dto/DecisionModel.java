package cloudify.widget.pool.manager.dto;

import cloudify.widget.pool.manager.node_management.DecisionDetails;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 8:20 PM
 */
public class DecisionModel {

    public static final int INITIAL_ID = -1;

    public long id = INITIAL_ID;
    public DecisionType decisionType;
    public String poolId;
    public boolean approved;
    public DecisionDetails details;

    public DecisionModel setId(long id) {
        this.id = id;
        return this;
    }

    public DecisionModel setDecisionType(DecisionType decisionType) {
        this.decisionType = decisionType;
        return this;
    }

    public DecisionModel setPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    public DecisionModel setApproved(boolean approved) {
        this.approved = approved;
        return this;
    }

    public DecisionModel setDetails(DecisionDetails details) {
        this.details = details;
        return this;
    }

    @Override
    public String toString() {
        return "DecisionModel{" +
                "id=" + id +
                ", decisionType='" + decisionType + '\'' +
                ", poolId='" + poolId + '\'' +
                ", approved=" + approved +
                ", details=" + details +
                '}';
    }

}
