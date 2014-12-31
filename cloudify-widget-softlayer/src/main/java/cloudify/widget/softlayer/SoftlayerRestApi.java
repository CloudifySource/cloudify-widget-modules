package cloudify.widget.softlayer;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by sefi on 30/12/14.
 */
public class SoftlayerRestApi {

    @Autowired
    private SoftlayerConnectDetails connectDetails;

    @Autowired
    private SoftlayerCatalogManager catalogManager;

    public SoftlayerRestApi() {
    }

    public JsonNode buildTemplate(SoftlayerMachineOptions softlayerMachineOptions) {
        JsonNode template = new JsonNode(catalogManager.getMachineTemplate());
        JSONObject parameters = template.getObject().getJSONArray("parameters").getJSONObject(0);

        catalogManager.appendPricesIds(softlayerMachineOptions.getHardwareId(), parameters.getJSONArray("prices"));
        parameters.put("location", softlayerMachineOptions.getLocationId());
        parameters.getJSONArray("virtualGuests").getJSONObject(0).put("hostname", softlayerMachineOptions.name());

        return template;
    }

    public JSONObject createNode(JsonNode template) throws Exception {
        verifyTemplate(template);

        HttpResponse<JsonNode> response = Unirest.post("https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/placeOrder.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .body(template)
                .asJson();

        if (response.getStatus() != 200) {
            String error = response.getBody().getObject().getString("error");
            throw new Exception(error);
        }

        JSONObject nodeMetadata = response.getBody().getObject();
        long guestId = nodeMetadata.getJSONObject("orderDetails").getJSONArray("virtualGuests").getJSONObject(0).getLong("id");
        int retryCount = 0;

        while (retryCount < 60) {
            // as long as we didnt try for more than an hour - keep trying.
            retryCount++;

            HttpResponse<JsonNode> activeTransactions = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}/getActiveTransactions.json")
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

        // add ssh details to the nodeMetadata
        JSONObject sshDetails = new JSONObject();
        HttpResponse<JsonNode> guestDetails = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}/getObject.json?objectMask=operatingSystem.passwords")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .routeParam("VIRTUAL_GUEST_ID", String.valueOf(guestId))
                .asJson();

        JSONObject guestDetailsObject = guestDetails.getBody().getObject();
        sshDetails.put("primaryIP", guestDetailsObject.getString("primaryIpAddress"));
        sshDetails.put("port", 22);
        JSONObject passwordsObject = guestDetailsObject.getJSONObject("operatingSystem").getJSONArray("passwords").getJSONObject(0);
        sshDetails.put("username", passwordsObject.getString("username"));
        sshDetails.put("password", passwordsObject.getString("password"));
        nodeMetadata.put("sshDetails", sshDetails);

        return nodeMetadata;

    }

    public Set<JSONObject> createNodes(JsonNode template, int nodesCount) throws Exception {
        Set<JSONObject> newNodes = new HashSet<JSONObject>();

        for (int i = 0; i < nodesCount; i++) {
            newNodes.add(createNode(template));
        }

        return newNodes;
    }

    public void destroyNode(String id) throws Exception {
        HttpResponse<JsonNode> billingItem = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}/getBillingItem.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .routeParam("VIRTUAL_GUEST_ID", id)
                .asJson();

        if (billingItem.getStatus() != 200) {
            String error = billingItem.getBody().getObject().getString("error");
            throw new Exception(error);
        }

        long billingId = billingItem.getBody().getObject().getLong("id");

        HttpResponse<String> cancelResult = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Billing_Item/{BILLING_ITEM_ID}/cancelService.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .routeParam("BILLING_ITEM_ID", String.valueOf(billingId))
                .asString();

        if (cancelResult.getStatus() != 200) {
            String error = billingItem.getBody().getObject().getString("error");
            throw new Exception(error);
        }

    }

    public void verifyTemplate(JsonNode template) throws Exception {
        HttpResponse<JsonNode> verifyTemplateResult = Unirest.post("https://api.softlayer.com/rest/v3/SoftLayer_Product_Order/verifyOrder.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .body(template)
                .asJson();

        if (verifyTemplateResult.getStatus() != 200) {
            String error = verifyTemplateResult.getBody().getObject().getString("error");
            throw new Exception(error);
        }
    }

}
