package cloudify.widget.ec2;

import cloudify.widget.api.clouds.ICloudServerStatus;

/**
 * User: eliranm
 * Date: 2/11/14
 * Time: 3:39 PM
 */
public enum Ec2CloudServerStatus implements ICloudServerStatus {

    // TODO list actual statuses from EC2 cloud API
    ACTIVE, BUILD, REBUILD, SUSPENDED, PAUSED, RESIZE, VERIFY_RESIZE, REVERT_RESIZE,
    PASSWORD, REBOOT, HARD_REBOOT, DELETED, UNKNOWN, ERROR, STOPPED, UNRECOGNIZED;

    public String value() {
        return name();
    }

    public static Ec2CloudServerStatus fromValue(String v) {
        try {
            return valueOf(v.replaceAll("\\(.*", ""));
        } catch (IllegalArgumentException e) {
            return UNRECOGNIZED;
        }
    }

}
