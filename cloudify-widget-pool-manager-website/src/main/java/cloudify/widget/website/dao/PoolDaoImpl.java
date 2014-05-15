package cloudify.widget.website.dao;

import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.website.dao.mappers.PoolRowMapper;
import cloudify.widget.website.models.AccountModel;
import cloudify.widget.website.models.PoolConfigurationModel;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: evgenyf
 * Date: 2/27/14
 */
public class PoolDaoImpl implements IPoolDao {

    private static final String TABLE_NAME = "pool_configuration";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;

    private final static String delQueryById = "delete from " + TABLE_NAME + " where id = ?";
    private final static String delQueryByIdAndAccountId = "delete from " + TABLE_NAME + " where id = ? and account_id = ?";
    private final static String selectSqlById = "select * from " + TABLE_NAME + " where id = ?";
    private final static String selectSqlByAccountId = "select * from " + TABLE_NAME + " where account_id = ? and id = ?";
    private final static String selectAllByAccountId = "select * from " + TABLE_NAME + " where account_id = ?";
    private final static String selectSqlByPoolId = "select * from " + TABLE_NAME + " where id = ?";
    private final static String selectAll = "select * from " + TABLE_NAME;
    private final static String updateByIdAndAccountId = "update " + TABLE_NAME +
                                                                " set pool_setting = ? where id = ? and account_id = ?";

    private static final Logger logger = LoggerFactory.getLogger(PoolDaoImpl.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final PoolRowMapper poolRowMapper = new PoolRowMapper( objectMapper );

    static{
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert( jdbcTemplate ).
                withTableName(TABLE_NAME).
                usingGeneratedKeyColumns("id");
    }

    @Override
    public Long createPool(PoolConfigurationModel poolConfiguration) {

        Long accountId = poolConfiguration.getAccountId();
        PoolSettings poolSettings = poolConfiguration.getPoolSettings();
        String poolSettingsJson = null;
        try {
            poolSettingsJson = objectMapper.writeValueAsString( poolSettings );
        } catch (IOException e) {
            if( logger.isErrorEnabled() ){
                logger.error( "Unable to parse poolSetting to JSON", e );
            }
        }

        return createPool( accountId, poolSettingsJson );
    }

    @Override
    public Long createPool( Long accountId, String poolSettingsJson ) {

        PoolSettings poolSettings = null;
        try {
            poolSettings = objectMapper.readValue(poolSettingsJson, PoolSettings.class);
            if (poolSettingsJson.contains("uuid")) {
                logger.warn("uuid was received in pool settings json. it will not be persisted, and a new uuid will be generated.");
                poolSettings.regenerateUuid();
            }
            poolSettingsJson = objectMapper.writeValueAsString(poolSettings);
        } catch (IOException e) {
            logger.error("failed to map pool settings from json string", e);
            e.printStackTrace();
        }

        Map<String, Object> parametersMap = new HashMap<String, Object>(2);
        parametersMap.put("account_id", accountId);
        parametersMap.put("pool_setting", poolSettingsJson);
        if (poolSettings != null) {
            parametersMap.put("uuid", poolSettings.getUuid());
        }

        Number id = jdbcInsert.executeAndReturnKey(parametersMap);

        return (Long) id;
    }

    @Override
    public boolean updatePool( PoolConfigurationModel poolSettings ) {
        Long accountId = poolSettings.getAccountId();
        Long id = poolSettings.getId();
        PoolSettings settings = poolSettings.getPoolSettings();
        String poolSettingsJson = parsePoolSettingToJson(settings);

        return updatePool( id, accountId, poolSettingsJson );
    }

    @Override
    public boolean updatePool( Long id, Long accountId, String poolSettingsJson ) {

        int numOfRows = jdbcTemplate.update( updateByIdAndAccountId, poolSettingsJson, id, accountId );
        return numOfRows > 0;
    }

    @Override
    public boolean deletePool( Long id ) {
        int count = jdbcTemplate.update(delQueryById, new Object[]{id});
        return count > 0;
    }

    @Override
    public boolean deletePool( Long poolId, Long accountId ) {
        int count = jdbcTemplate.update(delQueryByIdAndAccountId, new Object[]{poolId, accountId});
        return count > 0;
    }

/*
//    @Override
    public PoolConfigurationModel readPool(Long id) {
        logger.info( "select query is [{}]", selectSqlById );
        PoolConfigurationModel poolConfigurationModel =( PoolConfigurationModel )
                jdbcTemplate.queryForObject(selectSqlById, new Object[]{id}, poolRowMapper );
        return poolConfigurationModel;
    }
*/

    @Override
    public PoolConfigurationModel readPoolByIdAndAccountId( Long poolId, Long accountId) {
        logger.debug( "select query is [{}] accountId [{}] poolId [{}]", selectSqlByAccountId, accountId, poolId );
        PoolConfigurationModel poolConfigurationModel =( PoolConfigurationModel )jdbcTemplate.queryForObject(
                selectSqlByAccountId, new Object[]{accountId, poolId }, poolRowMapper );
        return poolConfigurationModel;
    }

    @Override
    public PoolConfigurationModel readPoolById( Long poolId ) {
        logger.debug( "select query is [{}] poolId [{}]", selectSqlByPoolId, poolId );
        PoolConfigurationModel poolConfigurationModel =( PoolConfigurationModel )jdbcTemplate.queryForObject(
                selectSqlByPoolId, new Object[]{ poolId }, poolRowMapper );
        return poolConfigurationModel;
    }

    @Override
    public List<PoolConfigurationModel> readPools(AccountModel accountModel) {
        Long accountId = accountModel.id;
        return readPools(accountId);
    }

    @Override
    public List<PoolConfigurationModel> readPools(Long accountId) {
        logger.debug( "select query is [{}] accountId [{}]", selectAllByAccountId, accountId );
        List<PoolConfigurationModel> pools =  jdbcTemplate.query(
                selectAllByAccountId, new Object[]{accountId}, poolRowMapper );
        return pools;
    }

    @Override
    public List<PoolConfigurationModel> readPools() {
        logger.debug( "select query is [{}]", selectAll );
        List<PoolConfigurationModel> pools =  jdbcTemplate.query( selectAll, poolRowMapper );
        return pools;
    }

    private static String parsePoolSettingToJson( PoolSettings poolSettings ){

        String poolSettingsJson = null;
        try {
            poolSettingsJson = objectMapper.writeValueAsString( poolSettings );
        } catch (IOException e) {
            if( logger.isErrorEnabled() ){
                logger.error( "Unable to parse poolSetting to JSON", e );
            }
        }
        return poolSettingsJson;
    }

    private static PoolSettings parseJsonToPoolSetting( String poolSettingsJson ){

        PoolSettings poolSettings = null;
        try {
            poolSettings = objectMapper.readValue( poolSettingsJson, PoolSettings.class );
        } catch (IOException e) {
            if( logger.isErrorEnabled() ){
                logger.error( "Unable to read JSON to PoolSettings instance", e );
            }
        }
        return poolSettings;
    }
}
  