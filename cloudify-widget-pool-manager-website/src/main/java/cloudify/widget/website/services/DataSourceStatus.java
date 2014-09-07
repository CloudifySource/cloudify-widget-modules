package cloudify.widget.website.services;

/**
 * Created by sefi on 9/7/14.
 */
public class DataSourceStatus {

    private boolean defaultAutoCommit;
    private boolean defaultReadOnly;
    private int initialSize;
    private int maxActive;
    private int maxIdle;
    private int maxOpenPreparedStatements;
    private long maxWait;
    private long minEvictableIdleTimeMillis;
    private int minIdle;
    private int numIdle;
    private int numActive;
    private int numTestsPerEvictionRun;
    private String password;
    private boolean removeAbandoned;
    private int removeAbandonedTimeout;
    private boolean testOnBorrow;
    private boolean testOnReturn;
    private boolean testWhileIdle;
    private long timeBetweenEvictionRunsMillis;
    private String url;
    private String username;
    private String validationQuery;
    private int validationQueryTimeout;
    private boolean closed;
    private boolean poolPreparedStatements;

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        this.defaultAutoCommit = defaultAutoCommit;
    }

    public boolean isDefaultAutoCommit() {
        return defaultAutoCommit;
    }

    public void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setMaxActive(int maxActive) {
        this.maxActive = maxActive;
    }

    public int getMaxActive() {
        return maxActive;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxOpenPreparedStatements(int maxOpenPreparedStatements) {
        this.maxOpenPreparedStatements = maxOpenPreparedStatements;
    }

    public int getMaxOpenPreparedStatements() {
        return maxOpenPreparedStatements;
    }

    public void setMaxWait(long maxWait) {
        this.maxWait = maxWait;
    }

    public long getMaxWait() {
        return maxWait;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setNumIdle(int numIdle) {
        this.numIdle = numIdle;
    }

    public int getNumIdle() {
        return numIdle;
    }

    public void setNumActive(int numActive) {
        this.numActive = numActive;
    }

    public int getNumActive() {
        return numActive;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandonedTimeout(int removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public int getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQueryTimeout(int validationQueryTimeout) {
        this.validationQueryTimeout = validationQueryTimeout;
    }

    public int getValidationQueryTimeout() {
        return validationQueryTimeout;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setPoolPreparedStatements(boolean poolPreparedStatements) {
        this.poolPreparedStatements = poolPreparedStatements;
    }

    public boolean isPoolPreparedStatements() {
        return poolPreparedStatements;
    }
}
