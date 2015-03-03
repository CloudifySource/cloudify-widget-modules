package cloudify.widget.softlayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * This class's sole purpose is to convert {@link org.json.JSONObject}s into the appropriate POJOs
 * or {@link org.json.JSONArray}s into the appropriate POJO collections
 *
 *
 * Created by sefi on 03/03/15.
 */
public class SoftlayerCodec {

    public static ArrayList<SoftlayerDataCenter> convertToDataCenters(JSONArray theArray) {
        ArrayList<SoftlayerDataCenter> dataCenters = new ArrayList<SoftlayerDataCenter>();

        for (int i = 0; i < theArray.length(); i++) {
            JSONObject item = theArray.getJSONObject(i);
            dataCenters.add(new SoftlayerDataCenter(item.getLong("id"), item.getString("name"), item.getString("longName")));
        }

        return dataCenters;
    }
}
