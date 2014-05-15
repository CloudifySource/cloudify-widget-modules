package cloudify.widget.pool.manager.dto;

import java.util.ArrayList;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 5:15 PM
 */
public class PoolsSettingsList extends ArrayList<PoolSettings> {

    public PoolSettings getById(String id) {
        if (id == null) {
            return null;
        }

        iterator();

        for (PoolSettings poolSettings : this) {
            if (id.equals(poolSettings.getUuid())) {
                return poolSettings;
            }
        }
        return null;
    }

    public PoolSettings getByProviderName(ProviderSettings.ProviderName providerName) {
        if (providerName == null) {
            return null;
        }
        for (PoolSettings poolSettings : this) {
            ProviderSettings.ProviderName name = null;
            ProviderSettings provider = poolSettings.getProvider();
            if (provider != null) {
                name = provider.getName();
            }
            if (providerName == name) {
                return poolSettings;
            }
        }
        return null;
    }
}
