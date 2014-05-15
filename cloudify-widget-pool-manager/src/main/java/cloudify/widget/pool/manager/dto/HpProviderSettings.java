package cloudify.widget.pool.manager.dto;

import cloudify.widget.hpcloudcompute.HpCloudComputeConnectDetails;
import cloudify.widget.hpcloudcompute.HpCloudComputeMachineOptions;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:22 PM
 */
public class HpProviderSettings extends ProviderSettings {

    public HpCloudComputeConnectDetails connectDetails;
    public HpCloudComputeMachineOptions machineOptions;

    public void setConnectDetails(HpCloudComputeConnectDetails connectDetails) {
        super.setConnectDetails(connectDetails);
    }

    public void setMachineOptions(HpCloudComputeMachineOptions machineOptions) {
        super.setMachineOptions(machineOptions);
    }
}
