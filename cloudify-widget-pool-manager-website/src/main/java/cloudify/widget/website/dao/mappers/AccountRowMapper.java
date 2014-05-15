package cloudify.widget.website.dao.mappers;

import cloudify.widget.website.models.AccountModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User: evgenyf
 * Date: 3/2/14
 */
public class AccountRowMapper implements RowMapper<AccountModel>{

    @Override
    public AccountModel mapRow(ResultSet rs, int rowNum) throws SQLException {

        AccountModel accountModel = new AccountModel();
        accountModel.setId( rs.getLong("id") );
        accountModel.setUuid( rs.getString("uuid") );

        return accountModel;
    }
}