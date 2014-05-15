package cloudify.widget.pool.manager.dto;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:21 PM
 */
public class ManagerSettings {

    private PoolsSettingsList pools = new PoolsSettingsList();

    public PoolsSettingsList getPools() {
        return pools;
    }

    public void setPools(PoolsSettingsList pools) {
        this.pools = pools;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ManagerSettings that = (ManagerSettings) o;
        if (!pools.equals(that.pools)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return pools.hashCode();
    }
}
