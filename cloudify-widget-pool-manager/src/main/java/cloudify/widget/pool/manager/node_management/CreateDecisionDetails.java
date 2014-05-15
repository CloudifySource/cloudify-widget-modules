package cloudify.widget.pool.manager.node_management;

import java.util.concurrent.atomic.AtomicInteger;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:31 PM
*/
public class CreateDecisionDetails implements DecisionDetails {

    private AtomicInteger _numInstances = new AtomicInteger(0);

    public int getNumInstances() {
        return _numInstances.get();
    }

    public CreateDecisionDetails setNumInstances(int instances) {
        _numInstances.set(instances);
        return this;
    }

    public CreateDecisionDetails incrementNumInstances() {
        _numInstances.getAndIncrement();
        return this;
    }

    public CreateDecisionDetails decrementNumInstances() {
        _numInstances.getAndDecrement();
        return this;
    }

    @Override
    public String toString() {
        return "CreateDecisionDetails{" +
                "_numInstances=" + _numInstances.get() +
                '}';
    }
}
