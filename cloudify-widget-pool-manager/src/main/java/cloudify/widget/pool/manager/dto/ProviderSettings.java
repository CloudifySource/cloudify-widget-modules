package cloudify.widget.pool.manager.dto;

import cloudify.widget.api.clouds.IConnectDetails;
import cloudify.widget.api.clouds.MachineOptions;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * <p>
 * <b>Notice:</b> we must send references of the classes annotated inside @JsonSubTypes to their superclass (this one)
 * or they will be shaded when accessing them from the superclass after deserialization. example:
 * </p>
 * <p>
 * we must do this in HpProviderSettings:
 * </p>
 * <pre>
 * public void setConnectDetails(HpCloudComputeConnectDetails connectDetails) {
 *     super.connectDetails = connectDetails;
 * }
 * </pre>
 * <p/>
 * <p>
 * or a null pointer exception will be thrown when calling:
 * </p>
 * <pre>
 * hpProviderSettings.connectDetails
 * </pre>
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name",
        visible = true) // we want the 'name' property to be in the output as well
@JsonSubTypes({
        @JsonSubTypes.Type(value = HpProviderSettings.class, name = "hp"),
        @JsonSubTypes.Type(value = Ec2ProviderSettings.class, name = "ec2"),
        @JsonSubTypes.Type(value = SoftlayerProviderSettings.class, name = "softlayer")})
public abstract class ProviderSettings {

    public static enum ProviderName {
        hp, softlayer, ec2;
    }

    private ProviderName name;
    private IConnectDetails connectDetails;
    private MachineOptions machineOptions;

    public ProviderName getName() {
        return name;
    }

    public void setName(ProviderName name) {
        this.name = name;
    }

    public IConnectDetails getConnectDetails() {
        return connectDetails;
    }

    public void setConnectDetails(IConnectDetails connectDetails) {
        this.connectDetails = connectDetails;
    }

    public MachineOptions getMachineOptions() {
        return machineOptions;
    }

    public void setMachineOptions(MachineOptions machineOptions) {
        this.machineOptions = machineOptions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProviderSettings that = (ProviderSettings) o;
        if (!connectDetails.equals(that.connectDetails)) return false;
        if (!machineOptions.equals(that.machineOptions)) return false;
        if (name != that.name) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + connectDetails.hashCode();
        result = 31 * result + machineOptions.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProviderSettings{" +
                "name=" + name +
                ", connectDetails=" + connectDetails +
                ", machineOptions=" + machineOptions +
                '}';
    }
}
