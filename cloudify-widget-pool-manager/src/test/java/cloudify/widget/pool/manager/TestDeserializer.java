package cloudify.widget.pool.manager;

import cloudify.widget.hpcloudcompute.HpCloudComputeConnectDetails;
import cloudify.widget.pool.manager.dto.ManagerSettings;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

/**
 * User: eliranm
 * Date: 2/26/14
 * Time: 4:51 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
@ActiveProfiles({"softlayer"})
public class TestDeserializer {

    private static Logger logger = LoggerFactory.getLogger(TestDeserializer.class);

    @Autowired
    private SettingsDataAccessManager settingsDataAccessManager;

    @Test
    public void testDeserializer() throws IOException {

        ManagerSettings managerSettings = settingsDataAccessManager.read();

        Assert.assertNotNull("manager settings should not be null", managerSettings);

        ProviderSettings hpProviderSettings = managerSettings.getPools().getByProviderName(ProviderSettings.ProviderName.hp).getProvider();

        Assert.assertNotNull(String.format("hp provider settings should be found in manager settings [%s]", managerSettings), hpProviderSettings);

        Assert.assertNotNull("hp provider connect details settings should not be null", hpProviderSettings.getConnectDetails());

        Assert.assertThat(
                String.format("connect details for hp provider should be of type [%s], but instead found type [%s]", HpCloudComputeConnectDetails.class, hpProviderSettings.getConnectDetails().getClass()),
                hpProviderSettings.getConnectDetails(),
                CoreMatchers.instanceOf(HpCloudComputeConnectDetails.class));
    }

    public void setSettingsDataAccessManager(SettingsDataAccessManager settingsDataAccessManager) {
        this.settingsDataAccessManager = settingsDataAccessManager;
    }
}
