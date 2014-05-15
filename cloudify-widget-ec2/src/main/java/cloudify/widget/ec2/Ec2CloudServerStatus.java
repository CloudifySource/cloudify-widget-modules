package cloudify.widget.ec2;

import cloudify.widget.api.clouds.ICloudServerStatus;

/**
 * User: eliranm
 * Date: 2/11/14
 * Time: 3:39 PM
 */
public enum Ec2CloudServerStatus implements ICloudServerStatus {

    PENDING, TERMINATED, SUSPENDED, RUNNING, ERROR, UNRECOGNIZED;

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