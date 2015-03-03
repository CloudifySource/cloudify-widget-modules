package cloudify.widget.softlayer;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by sefi on 22/12/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:softlayer-context.xml"})
public class SoftlayerApiTest {

    private static Logger logger = LoggerFactory.getLogger(SoftlayerNonDestructiveOperationsTest.class);

    @Autowired
    SoftlayerConnectDetails connectDetails;

    @Autowired
    SoftlayerCatalogManager catalogManager;

    @Autowired
    SoftlayerRestApi softlayerRestApi;

    JsonNode placeOrderBody = new JsonNode("{\"parameters\":[{\"complexType\":\"SoftLayer_Container_Product_Order\",\"packageId\":46,\"location\":\"168642\",\"prices\":[],\"virtualGuests\":[{\"hostname\":\"cloudify-widget-tests-1\",\"domain\":\"cloudify-widget-tests.org\",\"privateNetworkOnlyFlag\":false}],\"hardware\":[],\"quantity\":1,\"useHourlyPricing\":true,\"imageTemplateGlobalIdentifier\":\"\",\"imageTemplateId\":\"\"}]}");
    String hardwareId = "3909,860,1155,3876,188,439";

    private void verifyOrder(JsonNode template) {
        try {
            softlayerRestApi.verifyTemplate(template, connectDetails);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }
    }

    private void destroyNode(String id) {
        try {
            softlayerRestApi.destroyNode(id, connectDetails);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

    }

    @Test
    public void testRestApi() {
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Account/ActivePackages.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .asJson();
            Assert.assertNotNull(jsonResponse);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testVerifyOrder() {
        JsonNode template = new JsonNode("{\"parameters\":[{\"complexType\":\"SoftLayer_Container_Product_Order\",\"packageId\":46,\"location\":\"168642\",\"prices\":[{\"id\":25689},{\"id\":32985},{\"id\":32438},{\"id\":32578},{\"id\":24713},{\"id\":34183},{\"id\":34807},{\"id\":27023},{\"id\":32500},{\"id\":32627},{\"id\":23070},{\"id\":35310},{\"id\":33483}],\"virtualGuests\":[{\"hostname\":\"cloudify-widget-tests-1\",\"domain\":\"cloudify.org\",\"privateNetworkOnlyFlag\":false}],\"hardware\":[],\"quantity\":1,\"useHourlyPricing\":true,\"imageTemplateGlobalIdentifier\":\"\",\"imageTemplateId\":\"\"}]}");
        verifyOrder(template);
    }

//    @Test
//    public void testJSON() {
//        JSONArray prices = new JSONArray(catalogManager.getPricesJSONArrayTemplate());
//        Assert.assertTrue(true);
//    }

    @Test
    public void testCatalog() {
        JSONArray prices = placeOrderBody.getObject().getJSONArray("parameters").getJSONObject(0).getJSONArray("prices");

        Assert.assertEquals(0, prices.length());

        prices = catalogManager.appendPricesIds(hardwareId, prices, connectDetails);

        Assert.assertEquals(13, prices.length());
    }


    @Test
    public void testCreateNode() {
        SoftlayerMachineOptions softlayerMachineOptions = new SoftlayerMachineOptions();
        softlayerMachineOptions.setMask("cw-test-sefi");
        softlayerMachineOptions.setLocationId("352494");
        softlayerMachineOptions.setHardwareId(hardwareId);

        JsonNode template = softlayerRestApi.buildTemplate(softlayerMachineOptions, connectDetails);

        Assert.assertNotNull(template);
        JSONObject node = null;

        try {
            node = softlayerRestApi.createNode(template, connectDetails);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        Assert.assertNotNull(node);

        SoftlayerCloudServerCreated softlayerCloudServerCreated = new SoftlayerCloudServerCreated(node);

        Assert.assertNotNull(softlayerCloudServerCreated.getId());
        Assert.assertNotNull(softlayerCloudServerCreated.getSshDetails());

    }

    @Test
    /**
     * Can't be run automatically, because it depends on a valid guestId.
     */
    public void testDestroyNode() throws Exception {
        String guestId = "8284675";

        destroyNode(guestId);
    }

    @Test
    public void testGetByMask() throws Exception {
        ArrayList<JSONObject> nodes = softlayerRestApi.listByMask("sefi-test", connectDetails);

        Assert.assertNotNull(nodes);
    }

    @Test
    /**
     * test get data centers.
     */
    public void testGetDataCenters() throws Exception {
        ArrayList<SoftlayerDataCenter> dataCenters = softlayerRestApi.getDataCenters(connectDetails);
        Assert.assertNotNull(dataCenters);
    }

}
