package cloudify.widget.website.services;

import cloudify.widget.common.GsObjectMapper;
import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sefi on 9/7/14.
 */
public class DBStatusReporter {

    @Autowired
    private BasicDataSource websiteDataSource;

    @Autowired
    private BasicDataSource poolManagerDataSource;


    public Map<String, Object> getStatus () {
        HashMap<String, Object> dsStatuses = new HashMap<String, Object>();

        dsStatuses.put("websiteDS", getDataSourceStatusInstance(websiteDataSource));
        dsStatuses.put("poolManagetDS", getDataSourceStatusInstance(poolManagerDataSource));

        return dsStatuses;
    }

    private Object getDataSourceStatusInstance(BasicDataSource basicDataSource) {
        GsObjectMapper objectMapper = new GsObjectMapper();
        objectMapper.addMixInAnnotations(BasicDataSource.class, BasicDataSourceMixin.class);
        return objectMapper.valueToTree(basicDataSource);

    }

}
