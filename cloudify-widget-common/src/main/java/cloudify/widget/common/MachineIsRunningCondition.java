package cloudify.widget.common;

import cloudify.widget.api.clouds.CloudServer;

/**
 * User: evgenyf
 * Date: 2/12/14
 */
public class MachineIsRunningCondition implements WaitTimeout.Condition {
    public CloudServer machine;

    @Override
    public boolean apply() {
        return machine.isRunning();
    }

    public void setMachine(CloudServer machine) {
        this.machine = machine;
    }

    @Override
    public String toString() {
        return "MachineIsRunningCondition{machine=" + machine + "}";
    }
}
