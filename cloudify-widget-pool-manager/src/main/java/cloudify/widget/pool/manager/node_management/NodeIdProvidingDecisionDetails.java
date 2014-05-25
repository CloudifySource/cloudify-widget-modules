package cloudify.widget.pool.manager.node_management;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:31 PM
*/
public abstract class NodeIdProvidingDecisionDetails<T extends NodeIdProvidingDecisionDetails> implements DecisionDetails {

    private long _nodeId;

    public long getNodeId() {
        return _nodeId;
    }

    public T setNodeId(long nodeId) {
        _nodeId = nodeId;
        return (T) this;
    }

    @Override
    public String toString() {
        return getClass() + "{" +
                "_nodeId=" + _nodeId +
                '}';
    }
}
