package cloudify.widget.pool.manager.node_management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class BootstrapNodeManager extends NodeManager<BootstrapNodeManager> {

    private static Logger logger = LoggerFactory.getLogger(BootstrapNodeManager.class);

    @Override
    public BootstrapNodeManager decide() {
        return this;
    }

    @Override
    public BootstrapNodeManager execute() {
        return this;
    }
}
