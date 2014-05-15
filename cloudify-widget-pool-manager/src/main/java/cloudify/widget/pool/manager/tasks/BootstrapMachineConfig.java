package cloudify.widget.pool.manager.tasks;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 7:01 PM
 */
public interface BootstrapMachineConfig extends TaskConfig, NodeModelProvider {

    String getBootstrapScriptResourcePath();

}
