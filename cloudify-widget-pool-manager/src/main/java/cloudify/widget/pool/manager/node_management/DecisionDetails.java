package cloudify.widget.pool.manager.node_management;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:30 PM
*/
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CreateDecisionDetails.class, name = "createDecisionDetails"),
        @JsonSubTypes.Type(value = DeleteDecisionDetails.class, name = "deleteDecisionDetails"),
        @JsonSubTypes.Type(value = DeleteExpiredDecisionDetails.class, name = "deleteExpiredDecisionDetails"),
        @JsonSubTypes.Type(value = BootstrapDecisionDetails.class, name = "bootstrapDecisionDetails"),
        @JsonSubTypes.Type(value = PingDecisionDetails.class, name = "pingDecisionDetails")
})
public interface DecisionDetails {
}
