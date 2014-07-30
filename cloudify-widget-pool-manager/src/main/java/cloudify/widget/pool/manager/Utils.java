package cloudify.widget.pool.manager;

import cloudify.widget.common.GsObjectMapper;

import java.io.IOException;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 8:43 PM
 */
public class Utils {

    private Utils() {}

    public static String objectToJson( Object obj ){
        try {
            return new GsObjectMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException("unable to serialize obj to json " + obj , e);
        }
    }
}
