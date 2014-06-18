package cloudify.widget.hpcloudcompute;

import cloudify.widget.api.clouds.MachineOptions;

/**
 * User: evgenyf
 * Date: 2/9/14
 */
public class HpMachineOptions implements MachineOptions {

    private String mask;
    private int machinesCount;
    private String hardwareId;
    private String imageId;
    private String securityGroup = null;
    private String networkUuid;
    private String keyPairName;

    public HpMachineOptions(){}

    public HpMachineOptions(String mask){
        this(mask, 1 );
    }

    public HpMachineOptions(String mask, int machinesCount){
        this.mask = mask;
        this.machinesCount = machinesCount;
    }

    public HpMachineOptions setMask(String mask){
        this.mask = mask;
        return this;
    }

    public HpMachineOptions setMachinesCount( int machinesCount ){
        this.machinesCount = machinesCount;
        return this;
    }

    public HpMachineOptions setHardwareId( String hardwareId ){
        this.hardwareId = hardwareId;
        return this;
    }

    public HpMachineOptions setImageId( String imageId ){
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

    public String getNetworkUuid() {
        return networkUuid;
    }

    public void setNetworkUuid(String networkUuid) {
        this.networkUuid = networkUuid;
    }

    public String getKeyPairName() {
        return keyPairName;
    }

    public void setKeyPairName(String keyPairName) {
        this.keyPairName = keyPairName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HpMachineOptions that = (HpMachineOptions) o;

        if (machinesCount != that.machinesCount) return false;
        if (hardwareId != null ? !hardwareId.equals(that.hardwareId) : that.hardwareId != null) return false;
        if (imageId != null ? !imageId.equals(that.imageId) : that.imageId != null) return false;
        if (keyPairName != null ? !keyPairName.equals(that.keyPairName) : that.keyPairName != null) return false;
        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;
        if (networkUuid != null ? !networkUuid.equals(that.networkUuid) : that.networkUuid != null) return false;
        if (securityGroup != null ? !securityGroup.equals(that.securityGroup) : that.securityGroup != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mask != null ? mask.hashCode() : 0;
        result = 31 * result + machinesCount;
        result = 31 * result + (hardwareId != null ? hardwareId.hashCode() : 0);
        result = 31 * result + (imageId != null ? imageId.hashCode() : 0);
        result = 31 * result + (securityGroup != null ? securityGroup.hashCode() : 0);
        result = 31 * result + (networkUuid != null ? networkUuid.hashCode() : 0);
        result = 31 * result + (keyPairName != null ? keyPairName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "HpMachineOptions{" +
                "mask='" + mask + '\'' +
                ", machinesCount=" + machinesCount +
                ", hardwareId='" + hardwareId + '\'' +
                ", imageId='" + imageId + '\'' +
                ", securityGroup='" + securityGroup + '\'' +
                ", networkUuid='" + networkUuid + '\'' +
                ", keyPairName='" + keyPairName + '\'' +
                '}';
    }
}