package cloudify.widget.pool.manager.dto;

import cloudify.widget.hp.HpConnectDetails;
import cloudify.widget.hp.HpMachineOptions;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:22 PM
 */
public class HpFolsomProviderSettings extends ProviderSettings {

    public HpConnectDetails connectDetails;
    public HpMachineOptions machineOptions;

    public void setConnectDetails(HpConnectDetails connectDetails) {
        super.setConnectDetails(connectDetails);
    }

    public void setMachineOptions(HpMachineOptions machineOptions) {
        super.setMachineOptions(machineOptions);
    }
}
