package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.MachineOptions;
import org.jclouds.compute.domain.OsFamily;

/**
 * User: evgenyf
 * Date: 2/9/14
 */
public class SoftlayerMachineOptions implements MachineOptions {

    private String mask;
    private int machinesCount;
    private OsFamily osFamily;
    private String locationId;
    private String hardwareId;

    public SoftlayerMachineOptions() {
    }

    public SoftlayerMachineOptions setMask(String mask) {
        this.mask = mask;
        return this;
    }

    public String name() {
        return mask + "-" + System.currentTimeMillis();
    }

    public SoftlayerMachineOptions setMachinesCount(int machinesCount) {
        this.machinesCount = machinesCount;
        return this;
    }

    public int machinesCount() {
        return machinesCount;
    }

    public SoftlayerMachineOptions setLocationId(String locationId) {
        this.locationId = locationId;
        return this;
    }

    public SoftlayerMachineOptions setHardwareId(String hardwareId) {
        this.hardwareId = hardwareId;
        return this;
    }

    public SoftlayerMachineOptions setOsFamily(OsFamily osFamily) {
        this.osFamily = osFamily;
        return this;
    }

    public String getMask() {
        return mask;
    }

    public int getMachinesCount() {
        return machinesCount;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getHardwareId() {
        return hardwareId;
    }

    public OsFamily getOsFamily() {
        return osFamily;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoftlayerMachineOptions that = (SoftlayerMachineOptions) o;

        if (machinesCount != that.machinesCount) return false;
        if (hardwareId != null ? !hardwareId.equals(that.hardwareId) : that.hardwareId != null) return false;
        if (locationId != null ? !locationId.equals(that.locationId) : that.locationId != null) return false;
        if (osFamily != that.osFamily) return false;
        if (mask != null ? !mask.equals(that.mask) : that.mask != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mask != null ? mask.hashCode() : 0;
        result = 31 * result + machinesCount;
        result = 31 * result + (osFamily != null ? osFamily.hashCode() : 0);
        result = 31 * result + (locationId != null ? locationId.hashCode() : 0);
        result = 31 * result + (hardwareId != null ? hardwareId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SoftlayerMachineOptions{" +
                "mask='" + mask + '\'' +
                ", machinesCount=" + machinesCount +
                ", osFamily=" + osFamily +
                ", locationId='" + locationId + '\'' +
                ", hardwareId='" + hardwareId + '\'' +
                '}';
    }
}