package cloudify.widget.pool.manager.node_management;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 3:09 PM
 */
public interface DecisionMaker<T extends NodeManager> {

    public T decide();

    public T execute();
}
