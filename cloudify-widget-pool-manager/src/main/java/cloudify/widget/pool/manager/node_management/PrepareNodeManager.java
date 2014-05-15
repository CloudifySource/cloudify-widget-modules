package cloudify.widget.pool.manager.node_management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class PrepareNodeManager extends NodeManager<PrepareNodeManager> {

    private static Logger logger = LoggerFactory.getLogger(PrepareNodeManager.class);

    @Override
    public PrepareNodeManager decide() {
        return this;
    }

    @Override
    public PrepareNodeManager execute() {
        return this;
    }
}
