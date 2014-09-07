package cloudify.widget.website.services;

import org.apache.commons.dbcp.BasicDataSource;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sefi on 9/7/14.
 */
public class DBStatusReporter {
    private BasicDataSource websiteDataSource;
    private BasicDataSource poolManagerDataSource;


    public Map<String, DataSourceStatus> getStatus () {
        HashMap<String, DataSourceStatus> dsStatuses = new HashMap<String, DataSourceStatus>();

        dsStatuses.put("websiteDS", getDataSourceStatusInstance(websiteDataSource));
        dsStatuses.put("poolManagerDS", getDataSourceStatusInstance(poolManagerDataSource));

        return dsStatuses;
    }

    private DataSourceStatus getDataSourceStatusInstance(BasicDataSource basicDataSource) {
        DataSourceStatus dsStatus = new DataSourceStatus();

        dsStatus.setDefaultAutoCommit(basicDataSource.getDefaultAutoCommit());
        dsStatus.setDefaultReadOnly(basicDataSource.getDefaultReadOnly());
        dsStatus.setInitialSize(basicDataSource.getInitialSize());
        dsStatus.setMaxActive(basicDataSource.getMaxActive());
        dsStatus.setMaxIdle(basicDataSource.getMaxIdle());
        dsStatus.setMaxOpenPreparedStatements(basicDataSource.getMaxOpenPreparedStatements());
        dsStatus.setMaxWait(basicDataSource.getMaxWait());
        dsStatus.setMinEvictableIdleTimeMillis(basicDataSource.getMinEvictableIdleTimeMillis());
        dsStatus.setMinIdle(basicDataSource.getMinIdle());
        dsStatus.setNumIdle(basicDataSource.getNumIdle());
        dsStatus.setNumActive(basicDataSource.getNumActive());
        dsStatus.setNumTestsPerEvictionRun(basicDataSource.getNumTestsPerEvictionRun());
        dsStatus.setPassword(basicDataSource.getPassword());
        dsStatus.setRemoveAbandoned(basicDataSource.getRemoveAbandoned());
        dsStatus.setRemoveAbandonedTimeout(basicDataSource.getRemoveAbandonedTimeout());
        dsStatus.setTestOnBorrow(basicDataSource.getTestOnBorrow());
        dsStatus.setTestOnReturn(basicDataSource.getTestOnReturn());
        dsStatus.setTestWhileIdle(basicDataSource.getTestWhileIdle());
        dsStatus.setTimeBetweenEvictionRunsMillis(basicDataSource.getTimeBetweenEvictionRunsMillis());
        dsStatus.setUrl(basicDataSource.getUrl());
        dsStatus.setUsername(basicDataSource.getUsername());
        dsStatus.setValidationQuery(basicDataSource.getValidationQuery());
        dsStatus.setValidationQueryTimeout(basicDataSource.getValidationQueryTimeout());
        dsStatus.setClosed(basicDataSource.isClosed());
        dsStatus.setPoolPreparedStatements(basicDataSource.isPoolPreparedStatements());

        return dsStatus;
    }


    public void setWebsiteDataSource(BasicDataSource websiteDataSource) {
        this.websiteDataSource = websiteDataSource;
    }

    public void setPoolManagerDataSource(BasicDataSource poolManagerDataSource) {
        this.poolManagerDataSource = poolManagerDataSource;
    }
}
