package cloudify.widget.pool.manager.dto;

import cloudify.widget.pool.manager.node_management.NodeManagementMode;
import cloudify.widget.pool.manager.node_management.NodeManagementModuleType;

import java.util.List;

/**
 * User: eliranm
 * Date: 5/5/14
 * Time: 4:47 PM
 */
public class NodeManagementSettings {

    private NodeManagementMode mode;

    private List<NodeManagementModuleType> activeModules;

    private EmailSettings emailSettings;

    private List<PingSettings> pingSettings;

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

    public EmailSettings getEmailSettings() {
        return emailSettings;
    }

    public void setEmailSettings(EmailSettings emailSettings) {
        this.emailSettings = emailSettings;
    }

    public List<PingSettings> getPingSettings() {
        return pingSettings;
    }

    public void setPingSettings(List<PingSettings> pingSettings) {
        this.pingSettings = pingSettings;
    }
}
