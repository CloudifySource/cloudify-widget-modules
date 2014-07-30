package cloudify.widget.common;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * GS wrapper for Jackson's {@link com.fasterxml.jackson.databind.ObjectMapper}.
 * It provides the means to chain customizations on the ObjectMapper instance.
 * <p/>
 * Created by sefi on 7/29/14.
 */
public class GsObjectMapper extends ObjectMapper {

    public GsObjectMapper removeFailOnUnknownProperties() {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return this;
    }

    public GsObjectMapper addFailOnUnknownProperties() {
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        return this;
    }

}
