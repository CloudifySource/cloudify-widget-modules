package cloudify.widget.website.dao;

import cloudify.widget.website.models.AccountModel;
import cloudify.widget.website.models.PoolConfigurationModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/27/14
 * Time: 10:40 AM
 */
public interface IPoolDao {

    /**
     * saves model and returns ID
     * @param poolSettings
     * @return
     */
    public Long createPool( PoolConfigurationModel poolSettings );
    public Long createPool( Long accountId, String poolSettingsJson );

    public boolean updatePool( PoolConfigurationModel poolSettings );
    public boolean updatePool( Long id, Long accountId, String poolSettingsJson );

    public boolean deletePool( Long id );
    public boolean deletePool( Long id, Long accountId );

    public List<PoolConfigurationModel> readPools( AccountModel accountModel );
    public List<PoolConfigurationModel> readPools( Long accountId );

    public List<PoolConfigurationModel> readPools();

    public PoolConfigurationModel readPoolByIdAndAccountId( Long poolId, Long accountId );

    public PoolConfigurationModel readPoolById( Long poolId );
}
