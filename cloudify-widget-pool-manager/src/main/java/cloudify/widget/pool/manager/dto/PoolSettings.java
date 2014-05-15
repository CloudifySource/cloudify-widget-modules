package cloudify.widget.pool.manager.dto;

import java.util.UUID;

/**
 *
 * Please don't remove fields, use deprecation instead.
 *
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:21 PM
 */
public class PoolSettings {

    private String uuid = regenerateUuid();

    private String name;
    private String authKey;
    private int maxNodes;
    private int minNodes;
    private BootstrapProperties bootstrapProperties;
    private ProviderSettings provider;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthKey() {
        return authKey;
    }

    public void setAuthKey(String authKey) {
        this.authKey = authKey;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    public int getMinNodes() {
        return minNodes;
    }

    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }

    public ProviderSettings getProvider() {
        return provider;
    }

    public void setProvider(ProviderSettings provider) {
        this.provider = provider;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BootstrapProperties getBootstrapProperties() {
        return bootstrapProperties;
    }

    public void setBootstrapProperties(BootstrapProperties bootstrapProperties) {
        this.bootstrapProperties = bootstrapProperties;
    }

    public String regenerateUuid() {
        String uuid = UUID.randomUUID().toString();
        setUuid(uuid);
        return uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoolSettings that = (PoolSettings) o;

        if (!uuid.equals(that.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public String toString() {
        return "PoolSettings{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", maxNodes=" + maxNodes +
                ", minNodes=" + minNodes +
                ", bootstrapProperties=" + bootstrapProperties +
                ", provider=" + provider +
                '}';
    }
}
