package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.NodesDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: eliranm
 * Date: 4/24/14
 * Time: 11:09 PM
 */
public abstract class NodeManager<T extends NodeManager> implements DecisionMaker<T> {

    @Autowired
    protected DecisionsDao decisionsDao;

    @Autowired
    protected NodesDao nodesDao;

    private Constraints _constraints;

    public NodeManager having(Constraints constraints) {
        _constraints = constraints;
        return this;
    }

    public Constraints getConstraints() {
        if (_constraints == null) {
            throw new RuntimeException("no constraints found!");
        }
        return _constraints;
    }

}
