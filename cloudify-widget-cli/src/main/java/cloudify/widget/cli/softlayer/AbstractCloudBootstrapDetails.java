package cloudify.widget.cli.softlayer;

import cloudify.widget.cli.ICloudBootstrapDetails;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 4/1/14
 * Time: 3:24 PM
 */
public abstract class AbstractCloudBootstrapDetails implements ICloudBootstrapDetails {

    public String driverName;

    public String getDriverName() {
        return driverName;
    }

    @Override
    public void setCloudDriver(String name) {
        this.driverName = name;
    }

    @Override
    public String toString() {
        return "AbstractCloudBootstrapDetails{" +
                "driverName='" + driverName + '\'' +
                '}';
    }
}
