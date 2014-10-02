package cloudify.widget.pool.manager.actions;

import cloudify.widget.common.GsObjectMapper;
import cloudify.widget.pool.manager.dto.PingResponse;
import cloudify.widget.pool.manager.dto.PingSettings;
import cloudify.widget.pool.manager.dto.PingResult;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        PingResponse pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(pingResult.isWhiteListed());
    }

    @Test
    //todo: this test takes a few minutes so it should be an integration test.
    public void testWrongPort() {
        pingSettings.setUrl("http://$HOST:8099");
        pingSettings.setPingTimeout(1000);

        PingResponse pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(!pingResult.isWhiteListed());
    }

    @Test
    public void testWhiteList() {
        ArrayList<String> whiteList = new ArrayList<String>();
        whiteList.add("404");
        pingSettings.setWhiteList(whiteList);

        PingResponse pingResult = pingAction.ping("www.google.com", pingSettings);
        Assert.isTrue(!pingResult.isWhiteListed());
    }

    @Test
    public void testHttps() {
        pingSettings.setUrl("https://$HOST:443");
        PingResponse pingResult = pingAction.ping("ssl.gigaspaces.com", pingSettings);
        Assert.isTrue(pingResult.isWhiteListed());
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

        List<PingResponse> pingResponses = pingAction.pingAll("www.google.com", pingSettingsArrayList);
        PingResult pingResult = new PingResult();
        pingResult.setPingResponses(pingResponses);
        Assert.isTrue(pingResult.isAggregatedPingResponse());
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

        List<PingResponse> pingResponses = pingAction.pingAll("www.google.com", pingSettingsArrayList);
        PingResult pingResult = new PingResult();
        pingResult.setPingResponses(pingResponses);
        Assert.isTrue(!pingResult.isAggregatedPingResponse());
    }

    @Test
    public void testRowMapper() {
        GsObjectMapper objectMapper = new GsObjectMapper();
        String pingResultString = "{\"pingStatus\":\"PING_FAIL\",\"pingResponses\":[{\"responseCode\":-1,\"errorMessage\":\"connect timed out\",\"pingSettings\":{\"url\":\"http://$HOST:8099\",\"whiteList\":[\"200\"],\"retryCount\":5,\"pingTimeout\":5000},\"whiteListed\":false},{\"responseCode\":-1,\"errorMessage\":\"connect timed out\",\"pingSettings\":{\"url\":\"http://$HOST:9099\",\"whiteList\":[\"200\",\"202\"],\"retryCount\":5,\"pingTimeout\":5000},\"whiteListed\":false}],\"timestamp\":1412241198510,\"aggregatedPingResponses\":false}";
        try {
            PingResult pingResult = objectMapper.readValue(pingResultString, PingResult.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
