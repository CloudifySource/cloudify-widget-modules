package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class BootstrapNodeManagementModule extends BaseNodeManagementModule<BootstrapNodeManagementModule> {

    private static Logger logger = LoggerFactory.getLogger(BootstrapNodeManagementModule.class);

    @Override
    public BootstrapNodeManagementModule decide() {
        return this;
    }

    @Override
    public BootstrapNodeManagementModule execute() {
        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.BOOTSTRAP;
    }
}
