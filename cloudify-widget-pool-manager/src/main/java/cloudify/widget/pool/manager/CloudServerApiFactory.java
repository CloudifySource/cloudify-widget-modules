package cloudify.widget.pool.manager;

import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 3:10 PM
 */
public class CloudServerApiFactory implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(CloudServerApiFactory.class);

    private ApplicationContext applicationContext;

    private CloudServerApiFactory() {
    }

    /**
     * Creates a cloud server API according to provider name.
     *
     * @param providerName The desired provider name.
     * @return A concrete API using the desired provider, or {@code null} if no such provider found.
     */
    public CloudServerApi create(ProviderSettings.ProviderName providerName) {
        try {
            return applicationContext.getBean(providerName.toString() + "CloudServerApi", CloudServerApi.class );
        }catch(Exception e){
            throw new RuntimeException(String.format("failed to create cloud server api from provider name [%s]", providerName));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
