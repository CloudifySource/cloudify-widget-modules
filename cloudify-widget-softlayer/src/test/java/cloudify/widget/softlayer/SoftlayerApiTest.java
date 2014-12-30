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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

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

    JsonNode placeOrderBody = new JsonNode("{\"parameters\":[{\"complexType\":\"SoftLayer_Container_Product_Order\",\"packageId\":46,\"location\":\"168642\",\"prices\":[],\"virtualGuests\":[{\"hostname\":\"cloudify-widget-tests-1\",\"domain\":\"cloudify-widget-tests.org\",\"privateNetworkOnlyFlag\":false}],\"hardware\":[],\"quantity\":1,\"useHourlyPricing\":true,\"imageTemplateGlobalIdentifier\":\"\",\"imageTemplateId\":\"\"}]}");
    String hardwareId = "3909,860,1155,3876,188,439";
//    JsonNode placeOrderBody = new JsonNode("{\"parameters\":[{\"complexType\":\"SoftLayer_Container_Product_Order\",\"packageId\":46,\"location\":\"168642\",\"prices\":[{\"id\":25689},{\"id\":32985},{\"id\":32438},{\"id\":32578},{\"id\":24713},{\"id\":34183},{\"id\":34807},{\"id\":27023},{\"id\":32500},{\"id\":32627},{\"id\":23070},{\"id\":35310},{\"id\":33483}],\"virtualGuests\":[{\"hostname\":\"cloudify-widget-tests-1\",\"domain\":\"cloudify.org\",\"privateNetworkOnlyFlag\":false}],\"hardware\":[],\"quantity\":1,\"useHourlyPricing\":true,\"imageTemplateGlobalIdentifier\":\"\",\"imageTemplateId\":\"\"}]}");

    private void verifyOrder() {
        HttpResponse<JsonNode> verifyResponse = null;
        try {
            verifyResponse = Unirest.post("https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/verifyOrder.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .body(placeOrderBody)
                    .asJson();

            Assert.assertNotNull(verifyResponse);
        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

    private long placeOrder() throws Exception {
        HttpResponse<JsonNode> jsonResponse = Unirest.post("https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/placeOrder.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .body(placeOrderBody)
                .asJson();

        Assert.assertNotNull(jsonResponse);

        if (jsonResponse.getBody().getObject().has("error")) {
            String error = jsonResponse.getBody().getObject().getString("error");
            Assert.assertNull(error); // fail and show error message.
        }

        long guestId = jsonResponse.getBody().getObject().getJSONObject("orderDetails").getJSONArray("virtualGuests").getJSONObject(0).getLong("id");
        Assert.assertNotNull(guestId);

        Thread.sleep(10000);

        int retryCount = 0;

        while (retryCount < 60) {
            // as long as we didnt try for more than an hour - keep trying.
            retryCount++;

            HttpResponse<JsonNode> activeTransactions = null;
            activeTransactions = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}/getActiveTransactions.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .routeParam("VIRTUAL_GUEST_ID", String.valueOf(guestId))
                    .asJson();

            int length = activeTransactions.getBody().getArray().length();

            if (length == 0) {
                break;
            } else {
                // wait a minute.
                Thread.sleep(60000);
            }

        }

        if (retryCount > 60) {
            throw new Exception("request time out");
        }

        return guestId;
    }

    private void destroyNode(long guestId) {
        try {
            HttpResponse<JsonNode> billingItem = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}/getBillingItem.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .routeParam("VIRTUAL_GUEST_ID", String.valueOf(guestId))
                    .asJson();

            long billingId = billingItem.getBody().getObject().getLong("id");
            Assert.assertNotNull(billingId);

            HttpResponse<String> cancelResult = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Billing_Item/{BILLING_ITEM_ID}/cancelService.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .routeParam("BILLING_ITEM_ID", String.valueOf(billingId))
                    .asString();

            Assert.assertEquals("true", cancelResult.getBody());

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
        try {
            HttpResponse<JsonNode> jsonResponse = Unirest.post("https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/verifyOrder.json")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .body(placeOrderBody)
                    .asJson();
            Assert.assertNotNull(jsonResponse);
            if (jsonResponse.getBody().getObject().has("error")) {
                String error = jsonResponse.getBody().getObject().getString("error");
                Assert.assertNull(error);
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        }

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

        prices = catalogManager.appendPricesIds(hardwareId, prices);

        Assert.assertEquals(13, prices.length());
    }


    @Test
    public void testCreateNode() {
        JSONArray prices = placeOrderBody.getObject().getJSONArray("parameters").getJSONObject(0).getJSONArray("prices");
        catalogManager.appendPricesIds(hardwareId, prices);

        verifyOrder();

        try {
            placeOrder();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

    }

    @Test
    /**
     * Can't be run automatically, because it depends on a valid guestId.
     */
    public void testDestroyNode() throws Exception {
        long guestId = 7653350;

        destroyNode(guestId);
    }

    @Test
    public void testCreateAndDestroyNode() {
        JSONArray prices = placeOrderBody.getObject().getJSONArray("parameters").getJSONObject(0).getJSONArray("prices");
        catalogManager.appendPricesIds(hardwareId, prices);
        long guestId = 0;

        verifyOrder();

        try {
            guestId = placeOrder();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.assertTrue(false);
        }

        destroyNode(guestId);

    }

}
