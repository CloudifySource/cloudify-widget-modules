package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;

/**
 * User: eliranm
 * Date: 5/5/14
 * Time: 5:32 PM
 */
public interface ModuleTypeProvider {

    public NodeManagementModuleType getType();
}
