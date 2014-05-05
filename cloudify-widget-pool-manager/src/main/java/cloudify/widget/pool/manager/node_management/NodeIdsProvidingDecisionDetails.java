package cloudify.widget.pool.manager.node_management;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:31 PM
*/
public abstract class NodeIdsProvidingDecisionDetails implements DecisionDetails {

    private Set<Long> _nodeIds = Collections.synchronizedSet(new HashSet<Long>());

    public Set<Long> getNodeIds() {
        return _nodeIds;
    }

    public NodeIdsProvidingDecisionDetails setNodeIds(Set<Long> nodeIds) {
        _nodeIds = nodeIds;
        return this;
    }

    public NodeIdsProvidingDecisionDetails addNodeId(long nodeId) {
        _nodeIds.add(nodeId);
        return this;
    }

    public NodeIdsProvidingDecisionDetails removeNodeId(long nodeId) {
        _nodeIds.remove(nodeId);
        return this;
    }

    @Override
    public String toString() {
        return getClass() + "{" +
                "_nodeIds=" + _nodeIds +
                '}';
    }
}
