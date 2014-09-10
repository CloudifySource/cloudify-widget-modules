package cloudify.widget.pool.manager.actions;

import cloudify.widget.pool.manager.dto.PingSettings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;

/**
 * Created by sefi on 9/9/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
@ActiveProfiles({"softlayer", "dev"})
public class TestPingAction {

    private static final Logger logger = LoggerFactory.getLogger(TestPingAction.class);

    @Autowired
    private PingAction pingAction;
    private PingSettings pingSettings;

    @Before
    public void init() {
        pingSettings = new PingSettings();
        pingSettings.setUrl("http://$HOST:80");
        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("200");
        pingSettings.setWhiteList(whiteList);
    }

    @Test
    public void testSuccess() {
        Boolean pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(pingResult);
    }

    @Test
    //todo: this test takes a few minutes so it should be an integration test.
    public void testWrongPort() {
        pingSettings.setUrl("http://$HOST:8099");
        pingSettings.setPingTimeout(1000);

        Boolean pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(!pingResult);
    }

    @Test
    public void testWhiteList() {
        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("404");
        pingSettings.setWhiteList(whiteList);

        Boolean pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(!pingResult);
    }

    @Test
    public void testHttps() {
        pingSettings.setUrl("https://$HOST:443");
        Boolean pingResult = pingAction.ping("ssl.gigaspaces.com", pingSettings);
        Assert.isTrue(pingResult);
    }

    @Test
    public void testSuccessList() {
        ArrayList<PingSettings> pingSettingsArrayList = new ArrayList<PingSettings>();

        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("200");

        PingSettings ps1 = new PingSettings();
        ps1.setUrl("http://$HOST:80");
        ps1.setWhiteList(whiteList);
        pingSettingsArrayList.add(ps1);

        PingSettings ps2 = new PingSettings();
        ps2.setUrl("http://$HOST:80");
        ps2.setWhiteList(whiteList);
        pingSettingsArrayList.add(ps2);

        Boolean pingResult = pingAction.pingAll("www.google.com", pingSettingsArrayList);
        Assert.isTrue(pingResult);
    }

    @Test
    public void testFailList() {
        ArrayList<PingSettings> pingSettingsArrayList = new ArrayList<PingSettings>();

        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("200");

        PingSettings ps1 = new PingSettings();
        ps1.setUrl("http://$HOST:80");
        ps1.setWhiteList(whiteList);
        pingSettingsArrayList.add(ps1);

        PingSettings ps2 = new PingSettings();
        ps2.setUrl("http://$HOST:8099");
        ps2.setWhiteList(whiteList);
        pingSettingsArrayList.add(ps2);

        Boolean pingResult = pingAction.pingAll("www.google.com", pingSettingsArrayList);
        Assert.isTrue(!pingResult);
    }
}
