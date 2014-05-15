package cloudify.widget.ec2;

import cloudify.widget.api.clouds.MachineOptions;

/**
 * User: evgenyf
 * Date: 2/9/14
 */
public class Ec2MachineOptions implements MachineOptions {

    private String mask;
    private int machinesCount;
    private String locationId;
    private String hardwareId;
    private String imageId;

    public Ec2MachineOptions(){}

    public Ec2MachineOptions( String mask){
        this(mask, 1 );
    }

    public Ec2MachineOptions( String mask, int machinesCount ){
        this.mask = mask;
        this.machinesCount = machinesCount;
    }

    public String getMask() {
        return mask;
    }

    public Ec2MachineOptions setMask(String mask){
        this.mask = mask;
        return this;
    }

    public Ec2MachineOptions setMachinesCount( int machinesCount ){
        this.machinesCount = machinesCount;
        return this;
    }

    public int getMachinesCount() {
        return machinesCount;
    }

    public Ec2MachineOptions setLocationId( String locationId ){
        this.locationId = locationId;
        return this;
    }

    public String getLocationId(){
        return locationId;
    }

    public Ec2MachineOptions setHardwareId( String hardwareId ){
        this.hardwareId = hardwareId;
        return this;
    }

    public String getHardwareId(){
        return hardwareId;
    }

    public Ec2MachineOptions setImageId( String imageId ){
        this.imageId = imageId;
        return this;
    }

    public String getImageId(){
        return imageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ec2MachineOptions that = (Ec2MachineOptions) o;

        if (machinesCount != that.machinesCount) return false;
        if (hardwareId != null ? !hardwareId.equals(that.hardwareId) : that.hardwareId != null) return false;
        if (imageId != null ? !imageId.equals(that.imageId) : that.imageId != null) return false;
        if (locationId != null ? !locationId.equals(that.locationId) : that.locationId != null) return false;
        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mask != null ? mask.hashCode() : 0;
        result = 31 * result + machinesCount;
        result = 31 * result + (locationId != null ? locationId.hashCode() : 0);
        result = 31 * result + (hardwareId != null ? hardwareId.hashCode() : 0);
        result = 31 * result + (imageId != null ? imageId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Ec2MachineOptions{" +
                "mask='" + mask + '\'' +
                ", machinesCount=" + machinesCount +
                ", locationId='" + locationId + '\'' +
                ", hardwareId='" + hardwareId + '\'' +
                ", imageId='" + imageId + '\'' +
                '}';
    }
}