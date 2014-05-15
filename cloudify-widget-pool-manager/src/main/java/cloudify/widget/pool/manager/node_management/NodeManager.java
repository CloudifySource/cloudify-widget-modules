package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.NodesDao;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: eliranm
 * Date: 4/24/14
 * Time: 11:09 PM
 */
public abstract class NodeManager<T extends NodeManager> implements DecisionMaker<T> {

    public static enum Mode {
        AUTO_APPROVAL, MANUAL_APPROVAL, MANUAL
    }

    @Autowired
    protected DecisionsDao decisionsDao;

    @Autowired
    protected NodesDao nodesDao;

    private Constraints _constraints;

    // TODO
    // * delegate value from executor level
    // * extract to config
    // * what should be the default?
    protected Mode mode = Mode.MANUAL;

    public NodeManager mode(Mode mode) {
        this.mode = mode;
        return this;
    }

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
