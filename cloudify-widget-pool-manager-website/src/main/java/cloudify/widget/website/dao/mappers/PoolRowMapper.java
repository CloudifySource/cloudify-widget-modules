package cloudify.widget.website.dao.mappers;

import cloudify.widget.common.StringUtils;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.website.models.PoolConfigurationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: evgenyf
 * Date: 3/2/14
 */
public class PoolRowMapper implements RowMapper{

    private final ObjectMapper objectMapper;

    private static final Logger logger = LoggerFactory.getLogger(PoolRowMapper.class);

    public PoolRowMapper( ObjectMapper objectMapper ){
        this.objectMapper = objectMapper;
    }

    @Override
    public Object mapRow(ResultSet rs, int rowNum) throws SQLException {

        PoolConfigurationModel poolConfigurationModel = new PoolConfigurationModel();
        poolConfigurationModel.setId( rs.getLong("id") );
        poolConfigurationModel.setAccountId( rs.getLong( "account_id" ) );
        String poolSettingsJson = rs.getString( "pool_setting" );
        String uuid = rs.getString("uuid");
        PoolSettings poolSettings = null;
        if( !StringUtils.isEmpty( poolSettingsJson ) ) {
            try {
                poolSettings = objectMapper.readValue(poolSettingsJson, PoolSettings.class);
                poolSettings.setUuid(uuid);
            } catch (IOException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Unable to read Pool settings json, poolSettingsJson=" + poolSettingsJson, e);
                }

                throw new RuntimeException("Unable to read Pool settings json, poolSettingsJson=" + poolSettingsJson, e);
            }
        }

        poolConfigurationModel.setPoolSettings( poolSettings );


        return poolConfigurationModel;
    }
}