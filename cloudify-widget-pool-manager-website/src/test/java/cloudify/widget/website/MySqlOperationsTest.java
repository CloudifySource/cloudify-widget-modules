package cloudify.widget.website;

import cloudify.widget.common.CollectionUtils;
import cloudify.widget.pool.manager.dto.*;
import cloudify.widget.softlayer.SoftlayerConnectDetails;
import cloudify.widget.website.dao.IAccountDao;
import cloudify.widget.website.dao.IPoolDao;
import cloudify.widget.website.models.AccountModel;
import cloudify.widget.website.models.PoolConfigurationModel;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.UUID;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:context.xml"})
public class MySqlOperationsTest {

    private static Logger logger = LoggerFactory.getLogger(MySqlOperationsTest.class);

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private IPoolDao poolDao;


    @Test
    public void testPool() {

        AccountModel accountModel1 = createAccountModel();
        AccountModel accountModel2 = createAccountModel();

        Long accountId1 = accountDao.createAccount(accountModel1);
        accountModel1.setId(accountId1);
        logger.info( "Account 1 created, uuid [{}], id [{}]", accountModel1.getUuid(), accountId1 );

        Long accountId2 = accountDao.createAccount(accountModel2);
        accountModel2.setId(accountId2);
        logger.info( "Account 2 created, uuid [{}], id [{}]", accountModel2.getUuid(), accountId2 );

        PoolConfigurationModel poolModel1 = createPoolConfigurationModel(accountId1, ProviderSettings.ProviderName.softlayer);
        PoolConfigurationModel poolModel2 = createPoolConfigurationModel( accountId2, ProviderSettings.ProviderName.hp );

        Long poolId1 = poolDao.createPool(poolModel1);
        poolModel1.setId(poolId1);
        logger.info( "Pool 1 created, poolSettings [{}], account Id [{}], id [{}]",
                        "authKey1", poolModel1.getAccountId(), poolId1 );

        Long poolId2 = poolDao.createPool( poolModel2 );
        poolModel2.setId(poolId2);
        logger.info( "Pool 2 created, account Id [{}], id [{}]", poolModel2.getAccountId(), poolId2 );

        AccountModel readAccountModel1 = accountDao.readAccountByUuid(accountModel1.getUuid());
        Assert.assertEquals("AccountModel 1 after read is not as expected ", accountModel1, readAccountModel1 );

        AccountModel readAccountModel2 = accountDao.readAccountByUuid(accountModel2.getUuid());
        Assert.assertEquals("AccountModel 2 after read is not as expected ", accountModel2, readAccountModel2);

        PoolConfigurationModel poolConfigurationModel = poolDao.readPoolByIdAndAccountId(poolId1, accountId1);
        logger.info("Pool with id [{}] was [{}]", poolId1, poolConfigurationModel != null ? "found" : "not found");
        Assert.assertNotNull("retrieved PoolConfiguration id [" + poolId1 + "] should not be null", poolConfigurationModel);
        Assert.assertEquals( "Retrieved pool configuration 1 is not as expected", poolModel1, poolConfigurationModel );
        Assert.assertNotNull("Retrieved pool configuration 1 cannot be null", poolConfigurationModel.getPoolSettings());

        poolConfigurationModel = poolDao.readPoolByIdAndAccountId(poolId2, accountId2);
        logger.info("Pool with id [{}] was ", poolId2, poolConfigurationModel != null ? "found" : "not found");
        Assert.assertNotNull("retrieved PoolConfiguration id [" + poolId2 + "] should not be null", poolConfigurationModel);
        Assert.assertEquals( "Retrieved pool configuration 2 is not as expected", poolModel2, poolConfigurationModel );
        Assert.assertNotNull("Retrieved pool configuration 2 cannot be null", poolConfigurationModel.getPoolSettings());

        PoolConfigurationModel poolConfigurationModel3 = poolDao.readPoolByIdAndAccountId( poolModel1.getId(), poolModel1.getAccountId() );
        logger.info( "Pool with accountId [{}] was [{}]", poolModel1.getAccountId(), poolConfigurationModel3 != null ? "found" : "not found" );
        Assert.assertNotNull( "retrieved PoolConfiguration id [" + poolModel1.getAccountId() + "] should not be null", poolConfigurationModel3 );
        Assert.assertEquals( "Retrieved account is not as expected", poolModel1, poolConfigurationModel3 );
        Assert.assertNotNull("Retrieved pool configuration cannot be null", poolConfigurationModel3.getPoolSettings());


        List<PoolConfigurationModel> allPools = poolDao.readPools();
        Assert.assertFalse("All retrieved pools list cannot be empty", CollectionUtils.isEmpty( allPools ) );

        List<PoolConfigurationModel> accountPools = poolDao.readPools(accountId2);
        Assert.assertFalse("All retrieved account pools list cannot be empty", CollectionUtils.isEmpty( accountPools ) );
        Assert.assertEquals("Retrieved account pools list size must be 1", 1, CollectionUtils.size(accountPools) );


        ProviderSettings updatedProviderSettings = new HpProviderSettings();
        updatedProviderSettings.setName( ProviderSettings.ProviderName.hp );

        //was previously HP, change it to softlayer
        long testedPoolId = poolId2;
        PoolConfigurationModel updatedPoolModel1 = createPoolConfigurationModel( accountId2, ProviderSettings.ProviderName.softlayer );
        updatedPoolModel1.setId( testedPoolId );
        poolDao.updatePool( updatedPoolModel1 );

        PoolConfigurationModel updatedPoolConfigurationModel = poolDao.readPoolById( testedPoolId );
        logger.info( "Updated Pool with id [{}] was ", testedPoolId, updatedPoolConfigurationModel != null ? "found" : "not found" );
        Assert.assertEquals("Retrieved Pool configuration is not as expected", updatedPoolModel1, updatedPoolConfigurationModel);
        Assert.assertNotNull("Retrieved pool configuration cannot be null", updatedPoolConfigurationModel.getPoolSettings());

        boolean account1Deleted = accountDao.deleteAccount( accountId1 );
        Assert.assertTrue("Account with id [" + accountId1 + "] was not deleted", account1Deleted);

        boolean account2Deleted = accountDao.deleteAccount( accountId2 );
        Assert.assertTrue("Account with id [" + accountId2 + "] was not deleted", account2Deleted);

        boolean pool1Deleted = poolDao.deletePool(poolId1, accountId1);
        logger.info( "Pool with id [{}] was ", poolId1, pool1Deleted ? "deleted" : "not deleted" );
        Assert.assertTrue("PoolConfiguration with id [" + poolId1 + "] was not deleted", pool1Deleted);

        boolean pool2Deleted = poolDao.deletePool(poolId2);
        Assert.assertTrue("PoolConfiguration with id [" + poolId2 + "] was not deleted", pool2Deleted);
    }

    private PoolConfigurationModel createPoolConfigurationModel( long accountId, ProviderSettings.ProviderName providerName ){

        PoolConfigurationModel poolModel = new PoolConfigurationModel();
        poolModel.setAccountId( accountId );

        ManagerSettings managerSettings = new ManagerSettings();
        PoolsSettingsList poolsSettingsList = new PoolsSettingsList();

        PoolSettings poolSettings = new PoolSettings();
        poolSettings.setName("softlayer_pool");
        poolSettings.setMinNodes(4);
        poolSettings.setMaxNodes(6);
        poolSettings.setAuthKey("authKey");

        SoftlayerProviderSettings providerSettings = new SoftlayerProviderSettings();
        providerSettings.setName(ProviderSettings.ProviderName.softlayer);

        SoftlayerConnectDetails connectDetails = new SoftlayerConnectDetails();
        connectDetails.setKey("key");
        connectDetails.setUsername("username");
        connectDetails.setNetworkId("000");
        providerSettings.setConnectDetails(connectDetails);

        poolSettings.setProvider(providerSettings);
        poolsSettingsList.add(poolSettings);
        managerSettings.setPools(poolsSettingsList);

        poolModel.setPoolSettings( poolSettings );

        return poolModel;
    }

    private static AccountModel createAccountModel(){
        String accountUuid = UUID.randomUUID().toString();
        AccountModel accountModel = new AccountModel();
        accountModel.setUuid(accountUuid);

        return accountModel;
    }

}