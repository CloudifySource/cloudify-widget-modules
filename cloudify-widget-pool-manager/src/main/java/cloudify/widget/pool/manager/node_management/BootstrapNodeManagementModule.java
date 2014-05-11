package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;

/**
 * User: eliranm
 * Date: 5/9/14
 * Time: 8:26 PM
 */
public class BootstrapNodeManagementModule extends BaseNodeManagementModule<BootstrapNodeManagementModule, BootstrapDecisionDetails> {

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
