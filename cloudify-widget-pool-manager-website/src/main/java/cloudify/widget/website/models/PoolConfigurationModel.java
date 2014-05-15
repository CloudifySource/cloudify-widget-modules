package cloudify.widget.website.models;

import cloudify.widget.pool.manager.dto.PoolSettings;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/27/14
 * Time: 10:46 AM
 */
public class PoolConfigurationModel {

    public Long id;

    public PoolSettings poolSettings; //in db is of TEXT type

    public Long accountId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PoolSettings getPoolSettings() {
        return poolSettings;
    }

    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoolConfigurationModel that = (PoolConfigurationModel) o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (poolSettings != null ? !poolSettings.equals(that.poolSettings) : that.poolSettings != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (poolSettings != null ? poolSettings.hashCode() : 0);
        result = 31 * result + (accountId != null ? accountId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PoolConfigurationModel{id=" + id +", poolSettings=" + poolSettings +", accountId=" + accountId + '}';
    }
}