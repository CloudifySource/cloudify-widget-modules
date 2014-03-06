package cloudify.widget.website.dao;

import cloudify.widget.website.dao.mappers.AccountRowMapper;
import cloudify.widget.website.models.AccountModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.HashMap;
import java.util.Map;

/**
 * User: evgenyf
 * Date: 2/27/14
 */
public class AccountDaoImpl implements IAccountDao {

    private static final String TABLE_NAME = "account";
    private static final String delQuery = "delete from " + TABLE_NAME + " where id = ?";
    private static final String selectSql = "select * from " + TABLE_NAME + " where uuid = ?";

    private JdbcTemplate jdbcTemplate;
    private SimpleJdbcInsert jdbcInsert;

    private static final Logger logger = LoggerFactory.getLogger(AccountDaoImpl.class);

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {

        this.jdbcTemplate = jdbcTemplate;
        jdbcInsert = new SimpleJdbcInsert( jdbcTemplate ).withTableName(TABLE_NAME).usingGeneratedKeyColumns("id");
    }

    @Override
    public Long createAccount( AccountModel account ) {

        Map<String,Object> parametersMap = new HashMap<String,Object>(1);
        parametersMap.put( "uuid", account.getUuid() );

        Number id = jdbcInsert.executeAndReturnKey(parametersMap);

        return ( Long )id;
    }

    @Override
    public boolean deleteAccount( Long id ) {

        int count = jdbcTemplate.update(delQuery, new Object[]{id});
        return count > 0;
    }

    @Override
    public AccountModel readAccountByUuid( String uuid ) {
        logger.info( "select query is [{}] uuid [{}]", selectSql, uuid );
        AccountModel accountModel  =
                ( AccountModel )jdbcTemplate.queryForObject(selectSql, new Object[]{uuid}, new AccountRowMapper());
        return accountModel;
    }
}