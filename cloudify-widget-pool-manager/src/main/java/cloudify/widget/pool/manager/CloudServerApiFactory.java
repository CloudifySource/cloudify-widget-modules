package cloudify.widget.pool.manager;

import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.ec2.Ec2CloudServerApi;
import cloudify.widget.hpcloudcompute.HpCloudComputeCloudServerApi;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import cloudify.widget.softlayer.SoftlayerCloudServerApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 3:10 PM
 */
public class CloudServerApiFactory {

    private static Logger logger = LoggerFactory.getLogger(CloudServerApiFactory.class);

    private CloudServerApiFactory() {
    }

    /**
     * Creates a cloud server API according to provider name.
     *
     * @param providerName The desired provider name.
     * @return A concrete API using the desired provider, or {@code null} if no such provider found.
     */
    public static CloudServerApi create(ProviderSettings.ProviderName providerName) {
        logger.trace("creating cloud server api implementation for provider [{}]", providerName.name());
        if (ProviderSettings.ProviderName.hp == providerName) {
            return new HpCloudComputeCloudServerApi();
        }
        if (ProviderSettings.ProviderName.softlayer == providerName) {
            return new SoftlayerCloudServerApi();
        }
        if (ProviderSettings.ProviderName.ec2 == providerName) {
            return new Ec2CloudServerApi();
        }
        logger.error("failed to create cloud server api, returning null");
        return null;
    }
}
