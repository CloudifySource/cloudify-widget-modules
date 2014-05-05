package cloudify.widget.pool.manager.dto;

import cloudify.widget.pool.manager.node_management.NodeManagementMode;

import java.util.List;

/**
 * User: eliranm
 * Date: 5/5/14
 * Time: 4:47 PM
 */
public class NodeManagementSettings {

    private NodeManagementMode mode;

    private List<NodeManagementModuleType> activeModules;

    public NodeManagementMode getMode() {
        return mode;
    }

    public void setMode(NodeManagementMode mode) {
        this.mode = mode;
    }

    public List<NodeManagementModuleType> getActiveModules() {
        return activeModules;
    }

    public void setActiveModules(List<NodeManagementModuleType> activeModules) {
        this.activeModules = activeModules;
    }

}
