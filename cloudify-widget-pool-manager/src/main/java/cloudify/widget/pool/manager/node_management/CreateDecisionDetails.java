package cloudify.widget.pool.manager.node_management;

/**
* User: eliranm
* Date: 4/29/14
* Time: 11:31 PM
*/
public class CreateDecisionDetails implements DecisionDetails {

    private int _numInstances = 0;

    public int getNumInstances() {
        return _numInstances;
    }

    public CreateDecisionDetails setNumInstances(int instances) {
        _numInstances = instances;
        return this;
    }

    @Override
    public String toString() {
        return "CreateDecisionDetails{" +
                "_numInstances=" + _numInstances +
                '}';
    }
}
