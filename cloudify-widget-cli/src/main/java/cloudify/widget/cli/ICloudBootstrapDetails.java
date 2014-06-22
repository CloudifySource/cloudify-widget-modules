package cloudify.widget.cli;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/11/14
 * Time: 2:00 PM
 */
public interface ICloudBootstrapDetails {

    // allows override for the cloud driver.
    public void setCloudDriver( String name);
}
