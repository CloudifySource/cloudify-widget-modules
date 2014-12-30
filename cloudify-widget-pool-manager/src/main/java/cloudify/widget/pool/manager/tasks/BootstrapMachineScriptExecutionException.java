package cloudify.widget.pool.manager.tasks;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 12/30/14
 * Time: 6:26 PM
 */
public class BootstrapMachineScriptExecutionException extends RuntimeException {

    private String output;

    public BootstrapMachineScriptExecutionException() {
    }

    public BootstrapMachineScriptExecutionException(String message) {
        super(message);
    }

    public BootstrapMachineScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public BootstrapMachineScriptExecutionException(Throwable cause) {
        super(cause);
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
