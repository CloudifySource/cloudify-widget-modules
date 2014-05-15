package cloudify.widget.pool.manager.dto;

import cloudify.widget.ec2.Ec2ConnectDetails;
import cloudify.widget.ec2.Ec2MachineOptions;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:22 PM
 */
public class Ec2ProviderSettings extends ProviderSettings {

    public Ec2ConnectDetails connectDetails;
    public Ec2MachineOptions machineOptions;

    public void setConnectDetails(Ec2ConnectDetails connectDetails) {
        super.setConnectDetails(connectDetails);
    }

    public void setMachineOptions(Ec2MachineOptions machineOptions) {
        super.setMachineOptions(machineOptions);
    }
}
