package cloudify.widget.hpcloudcompute;

import cloudify.widget.api.clouds.MachineOptions;

import java.util.Arrays;

/**
 * User: evgenyf
 * Date: 2/9/14
 */
public class HpCloudComputeMachineOptions implements MachineOptions {

    private String mask;
    private int machinesCount;
    private String hardwareId;
    private String imageId;
    private String securityGroup = null;

    public HpCloudComputeMachineOptions(){}

    public HpCloudComputeMachineOptions(String mask){
        this(mask, 1 );
    }

    public HpCloudComputeMachineOptions(String mask, int machinesCount){
        this.mask = mask;
        this.machinesCount = machinesCount;
    }

    public HpCloudComputeMachineOptions setMask(String mask){
        this.mask = mask;
        return this;
    }

    public HpCloudComputeMachineOptions setMachinesCount( int machinesCount ){
        this.machinesCount = machinesCount;
        return this;
    }

    public HpCloudComputeMachineOptions setHardwareId( String hardwareId ){
        this.hardwareId = hardwareId;
        return this;
    }

    public HpCloudComputeMachineOptions setImageId( String imageId ){
        this.imageId = imageId;
        return this;
    }

    public String getMask() {
        return mask;
    }

    public int getMachinesCount() {
        return machinesCount;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public String getImageId() {
        return imageId;
    }

    public String getSecurityGroup() {
        return securityGroup;
    }

    public void setSecurityGroup(String securityGroup) {
        this.securityGroup = securityGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HpCloudComputeMachineOptions that = (HpCloudComputeMachineOptions) o;

        if (machinesCount != that.machinesCount) return false;
        if (hardwareId != null ? !hardwareId.equals(that.hardwareId) : that.hardwareId != null) return false;
        if (imageId != null ? !imageId.equals(that.imageId) : that.imageId != null) return false;
        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mask != null ? mask.hashCode() : 0;
        result = 31 * result + machinesCount;
        result = 31 * result + (hardwareId != null ? hardwareId.hashCode() : 0);
        result = 31 * result + (imageId != null ? imageId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HpCloudComputeMachineOptions{" +
                "mask='" + mask + '\'' +
                ", machinesCount=" + machinesCount +
                ", hardwareId='" + hardwareId + '\'' +
                ", imageId='" + imageId + '\'' +
                '}';
    }
}