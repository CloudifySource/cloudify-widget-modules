package cloudify.widget.softlayer;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import junit.framework.Assert;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by sefi on 30/12/14.
 */
public class SoftlayerCatalogManager {

    public long stalePeriod;
    public String pricesJSONArrayTemplate;
    public String machineTemplate;

    private HashMap<String, JSONObject> itemsMap = new HashMap<String, JSONObject>();
    private Date lastCatalogUpdate = null;

    public SoftlayerCatalogManager() {
    }

    /**
     * Given a string comma delimited hardware IDs, this will convert them to the corresponding price IDs
     * and also include default price IDs required for the request to be successful.
     *
     * @param hardwareIds The comma delimited hardware IDs. For example "111,222,333,444".
     * @param connectDetails
     * @return a JSONArray of JSONObjects.
     */
    public JSONArray getPriceIds(String hardwareIds, SoftlayerConnectDetails connectDetails) {
        Date now = new Date();

        if (lastCatalogUpdate == null || now.getTime() - lastCatalogUpdate.getTime() >= getStalePeriod()) {
            // first execution or obsolete price map - go get it.
            updatePricesMap(connectDetails);
        }

        return convertHardwareIdsToPricesIds(hardwareIds);
    }

    /**
     * Similar to {@link: getPriceIds}, but it also accepts a JSONArray and appends the prices to it.
     *
     * @param hardwareIds The comma delimited hardware IDs. For example "111,222,333,444".
     * @param prices The JSONArray to be appended.
     * @param connectDetails
     * @return the updated prices JSONArray
     */
    public JSONArray appendPricesIds(String hardwareIds, JSONArray prices, SoftlayerConnectDetails connectDetails) {
        JSONArray hardwarePriceIds = getPriceIds(hardwareIds, connectDetails);

        for (int i = 0; i < hardwarePriceIds.length(); i++) {
            prices.put(hardwarePriceIds.getJSONObject(i));
        }

        return prices;
    }

    private void updatePricesMap(SoftlayerConnectDetails connectDetails) {
        HttpResponse<JsonNode> catalog = null;
        itemsMap = new HashMap<String, JSONObject>();

        try {
            // get the Softlayer catalog.
            catalog = Unirest.get("https://api.softlayer.com/rest/v3/SoftLayer_Product_Package/46.json?objectMask=items.prices%3blocations.locationAddress%3blocations.regions")
                    .basicAuth(connectDetails.getUsername(), connectDetails.getKey())
                    .asJson();

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        // fail if catalog response is null.
        Assert.assertNotNull(catalog);

        // create the prices map based on the catalog.
        for (int i = 0; i < catalog.getBody().getObject().getJSONArray("items").length(); i++) {
            JSONObject item = catalog.getBody().getObject().getJSONArray("items").getJSONObject(i);
            String id = String.valueOf(item.getLong("id"));

            itemsMap.put(id, item);
        }
    }

    private JSONArray convertHardwareIdsToPricesIds(String hardwareIds) {
        // first, create a prices JSONArray based on the default template.
        JSONArray prices = new JSONArray(getPricesJSONArrayTemplate());

        // Append prices that correlate to supplied hardware IDs.
        String[] hardwareItems = hardwareIds.split(",");
        for (String hardware : hardwareItems) {
            long priceItemId = itemsMap.get(hardware).getJSONArray("prices").getJSONObject(0).getLong("id");
            prices.put(new JSONObject("{\"id\":" + priceItemId + "}"));
        }

        return prices;
    }

    public long getStalePeriod() {
        return stalePeriod;
    }

    public void setStalePeriod(long stalePeriod) {
        this.stalePeriod = stalePeriod;
    }

    public String getPricesJSONArrayTemplate() {
        return pricesJSONArrayTemplate;
    }

    public void setPricesJSONArrayTemplate(String pricesJSONArrayTemplate) {
        this.pricesJSONArrayTemplate = pricesJSONArrayTemplate;
    }

    public String getMachineTemplate() {
        return machineTemplate;
    }

    public void setMachineTemplate(String machineTemplate) {
        this.machineTemplate = machineTemplate;
    }

}
