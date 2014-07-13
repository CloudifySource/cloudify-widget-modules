package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.ErrorModel;
import com.mysql.jdbc.Statement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 2:10 PM
 */
public class ErrorsDao {

    public static final String TABLE_NAME = "errors";
    public static final String COL_ERROR_ID = "id";
    public static final String COL_SOURCE = "source";
    public static final String COL_POOL_ID = "pool_id";
    public static final String COL_MESSAGE = "message";
    public static final String COL_INFO = "info";
    public static final String COL_TIMESTAMP = "timestamp";

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(final ErrorModel errorModel) {

        // used to hold the auto generated key in the 'id' column
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement ps = con.prepareStatement(
                                "insert into " + TABLE_NAME + " (" + COL_SOURCE + "," + COL_POOL_ID + "," + COL_MESSAGE + "," + COL_INFO + "," + COL_TIMESTAMP + ") values (?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, errorModel.source);
                        ps.setString(2, errorModel.poolId);
                        ps.setString(3, errorModel.message);
                        ps.setString(4, errorModel.info);
                        ps.setLong(5, errorModel.timestamp);
                        return ps;
                    }
                },
                keyHolder
        );

        // keep data integrity - fetch the last insert id and update the model
        errorModel.id = keyHolder.getKey().longValue();

        return affected > 0;
    }

    public List<ErrorModel> readAllOfPool(String poolId) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ? limit 200",
                new Object[]{poolId},
                new BeanPropertyRowMapper<ErrorModel>(ErrorModel.class));
    }

    public ErrorModel read(long errorId) {
        try {
            return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where " + COL_ERROR_ID + " = ?",
                    new Object[]{errorId},
                    new BeanPropertyRowMapper<ErrorModel>(ErrorModel.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(ErrorModel errorModel) {
        return jdbcTemplate.update(
                "update " + TABLE_NAME + " set " + COL_SOURCE + " = ?," + COL_POOL_ID + " = ?," + COL_MESSAGE + " = ?," + COL_INFO + " = ?," + COL_TIMESTAMP + " = ? where " + COL_ERROR_ID + " = ?",
                errorModel.source, errorModel.poolId, errorModel.message, errorModel.info, errorModel.timestamp, errorModel.id);
    }

    public int delete(long errorId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_ERROR_ID + " = ?", errorId);
    }

    public int deleteAllOfPool(String poolId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?", poolId);
    }

}
