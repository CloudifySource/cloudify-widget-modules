package cloudify.widget.softlayer;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sefi on 30/12/14.
 */
public class SoftlayerRestApi {

    @Autowired
    private SoftlayerCatalogManager catalogManager;

    public SoftlayerRestApi() {
    }

    /**
     * Returns a Softlayer machine template based on a wireframe defined in the {@link cloudify.widget.softlayer.SoftlayerCatalogManager} and
     * extended with information from the supplied {@link cloudify.widget.softlayer.SoftlayerMachineOptions} and {@link cloudify.widget.softlayer.SoftlayerConnectDetails}
     *
     * @param softlayerMachineOptions   The machine options
     * @param connectDetails    The connection details to use for the REST calls.
     * @return  The template {@link com.mashape.unirest.http.JsonNode}
     */
    public JsonNode buildTemplate(SoftlayerMachineOptions softlayerMachineOptions, SoftlayerConnectDetails connectDetails) {
        JsonNode template = new JsonNode(catalogManager.getMachineTemplate());
        JSONObject parameters = template.getObject().getJSONArray("parameters").getJSONObject(0);

        // Append the price IDs that corresponds with the supplied Hardware IDs to the templates' prices array.
        catalogManager.appendPricesIds(softlayerMachineOptions.getHardwareId(), parameters.getJSONArray("prices"), connectDetails);
        parameters.put("location", softlayerMachineOptions.getLocationId());
        parameters.getJSONArray("virtualGuests").getJSONObject(0).put("hostname", softlayerMachineOptions.name());

        return template;
    }

    public ArrayList<JSONObject> listByMask(String mask, SoftlayerConnectDetails connectDetails) throws Exception {
        ArrayList<JSONObject> maskedNodes = new ArrayList<JSONObject>();

        HttpResponse<JsonNode> response = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Account/VirtualGuests.json")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .asJson();

        if (response.getStatus() != 200) {
            String error = response.getBody().getObject().getString("error");
            throw new Exception(error);
        }

        JSONArray nodes = response.getBody().getArray();

        for (int i = 0; i < nodes.length(); i++) {
            JSONObject node = nodes.getJSONObject(i);
            if (node.getString("hostname").startsWith(mask)) {
                maskedNodes.add(node);
            }
        }

        return maskedNodes;
    }

    /**
     * Create a Softlayer machine node.
     * This is a long process that could take up to an hour, after which it is considered a failure and an Exception is thrown.
     *
     * @param template The template to use for creating the node
     * @param connectDetails The connection details to use for the REST calls.
     * @return The nodeMetadata {@link org.json.JSONObject}
     * @throws Exception Any exceptions will be thrown for external handling.
     */
    public JSONObject createNode(JsonNode template, SoftlayerConnectDetails connectDetails) throws Exception {
        verifyTemplate(template, connectDetails);   // verify before create. Fail exception is not handled here.

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

        // Softlayer takes it's sweet time until they start running the active transactions process, so we need to delay our polling.
        Thread.sleep(60000);

        // Softlayer machine creation is very slow and considered complete only when the active transactions list is empty.
        // as long as we didnt try for more than an hour - keep polling once a minute.
        while (retryCount < 60) {
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

        // Enrich nodeMetadata with ssh details. This requires yet another REST request...
        JSONObject sshDetails = new JSONObject();
        HttpResponse<JsonNode> guestDetails = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Virtual_Guest/{VIRTUAL_GUEST_ID}?objectMask=operatingSystem.passwords")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .routeParam("VIRTUAL_GUEST_ID", String.valueOf(guestId))
                .asJson();

        JSONObject guestDetailsObject = guestDetails.getBody().getObject();

        // Currently only SSH port 22 is supported, since Softlayer does not support custom ports OOTB.
        // If this is a machine based on an image with a custom SSH port, we will need to modify this.
        sshDetails.put("port", 22);
        sshDetails.put("primaryIP", guestDetailsObject.getString("primaryIpAddress"));
        JSONObject passwordsObject = guestDetailsObject.getJSONObject("operatingSystem").getJSONArray("passwords").getJSONObject(0);
        sshDetails.put("username", passwordsObject.getString("username"));
        sshDetails.put("password", passwordsObject.getString("password"));
        nodeMetadata.put("sshDetails", sshDetails);

        return nodeMetadata;
    }

    /**
     * Creates multiple nodes from a template. This calls createNode internally.
     * @see #createNode(com.mashape.unirest.http.JsonNode, SoftlayerConnectDetails)
     *
     * @param template The template
     * @param nodesCount The number of nodes to create
     * @param connectDetails The connection details to use for the REST calls.
     * @return  A {@link java.util.Set} of {@link org.json.JSONObject}s
     * @throws Exception    Any exceptions will be thrown for external handling.
     */
    public Set<JSONObject> createNodes(JsonNode template, int nodesCount, SoftlayerConnectDetails connectDetails) throws Exception {
        Set<JSONObject> newNodes = new HashSet<JSONObject>();

        for (int i = 0; i < nodesCount; i++) {
            newNodes.add(createNode(template, connectDetails));
        }

        return newNodes;
    }

    /**
     * Destroy a node.
     *
     * @param id The node id to destroy
     * @param connectDetails    The connection details to use for the REST calls.
     * @throws Exception        Any exceptions will be thrown for external handling.
     */
    public void destroyNode(String id, SoftlayerConnectDetails connectDetails) throws Exception {
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

    public JSONObject getNode(String id, SoftlayerConnectDetails connectDetails) throws Exception {
        HttpResponse<JsonNode> response = Unirest.get("https:/api.softlayer.com/rest/v3/SoftLayer_Account/VirtualGuests/{VIRTUAL_GUEST_ID}")
                .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                .routeParam("VIRTUAL_GUEST_ID", id)
                .asJson();

        if (response.getStatus() != 200) {
            String error = response.getBody().getObject().getString("error");
            throw new Exception(error);
        }

        return response.getBody().getObject();
    }

    /**
     * Verify the template.
     * This is required because the create process is very long and slow. To avoid trying to create machines with an invalid template
     * we first validate the template is valid.
     *
     * @param template  The template to validate.
     * @param connectDetails    The connection details to use for the REST calls.
     * @throws Exception    Throws exception on fail.
     */
    public void verifyTemplate(JsonNode template, SoftlayerConnectDetails connectDetails) throws Exception {
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
