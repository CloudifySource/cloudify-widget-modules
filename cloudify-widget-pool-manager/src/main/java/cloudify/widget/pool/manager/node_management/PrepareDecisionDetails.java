package cloudify.widget.pool.manager.node_management;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:31 PM
*/
public class PrepareDecisionDetails implements DecisionDetails {

    private Collection<String> _machineIds = new HashSet<String>();

    public Collection<String> getMachineIds() {
        return _machineIds;
    }

    public void setMachineIds(Collection<String> machineIds) {
        _machineIds = machineIds;
    }

    public PrepareDecisionDetails addMachineIds(Set<String> machineIds) {
        _machineIds.addAll(machineIds);
        return this;
    }

    @Override
    public String toString() {
        return "PrepareDecisionDetails{" +
                "_machineIds=" + _machineIds +
                '}';
    }
}
