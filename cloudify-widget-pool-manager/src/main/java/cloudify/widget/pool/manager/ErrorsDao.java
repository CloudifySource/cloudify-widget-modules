package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.ErrorModel;
import com.mysql.jdbc.Statement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import javax.sql.DataSource;
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
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_POOL_ID = "pool_id";
    public static final String COL_MESSAGE = "message";
    public static final String COL_INFO = "info";

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
                                "insert into " + TABLE_NAME + " (" + COL_TASK_NAME + "," + COL_POOL_ID + "," + COL_MESSAGE + "," + COL_INFO + ") values (?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, errorModel.taskName.name());
                        ps.setString(2, errorModel.poolId);
                        ps.setString(3, errorModel.message);
                        ps.setString(4, errorModel.info);
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
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?",
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
                "update " + TABLE_NAME + " set " + COL_TASK_NAME + " = ?," + COL_POOL_ID + " = ?," + COL_MESSAGE + " = ?," + COL_INFO + " = ? where " + COL_ERROR_ID + " = ?",
                errorModel.taskName.name(), errorModel.poolId, errorModel.message, errorModel.info, errorModel.id);
    }

    public int delete(long errorId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_ERROR_ID + " = ?", errorId);
    }

}
