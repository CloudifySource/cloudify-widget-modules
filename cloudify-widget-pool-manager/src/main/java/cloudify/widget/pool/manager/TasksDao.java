package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.TaskModel;
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
 * Date: 3/19/14
 * Time: 12:25 PM
 */
public class TasksDao implements ITasksDao {

    public static final String TABLE_NAME = "tasks";
    public static final String COL_TASK_ID = "id";
    public static final String COL_TASK_NAME = "task_name";
    public static final String COL_NODE_ID = "node_id";
    public static final String COL_POOL_ID = "pool_id";

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean create(final TaskModel taskModel) {

        // used to hold the auto generated key in the 'id' column
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement ps = con.prepareStatement(
                                "insert into " + TABLE_NAME + " (" + COL_TASK_NAME + "," + COL_NODE_ID + "," + COL_POOL_ID + ") values (?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, taskModel.taskName.name());
                        ps.setLong(2, taskModel.nodeId);
                        ps.setString(3, taskModel.poolId);
                        return ps;
                    }
                },
                keyHolder
        );

        // keep data integrity - fetch the last insert id and update the model
        taskModel.id = keyHolder.getKey().longValue();

        return affected > 0;
    }

    public List<TaskModel> readAllOfPool(String poolId) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?",
                new Object[]{poolId},
                new BeanPropertyRowMapper<TaskModel>(TaskModel.class));
    }

    public TaskModel read(long taskId) {
        try {
            return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where " + COL_TASK_ID + " = ?",
                    new Object[]{taskId},
                    new BeanPropertyRowMapper<TaskModel>(TaskModel.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(TaskModel taskModel) {
        return jdbcTemplate.update(
                "update " + TABLE_NAME + " set " + COL_TASK_NAME + " = ?," + COL_NODE_ID + " = ?," + COL_POOL_ID + " = ? where " + COL_TASK_ID + " = ?",
                taskModel.taskName.name(), taskModel.nodeId, taskModel.poolId, taskModel.id);
    }

    public int delete(long taskId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_TASK_ID + " = ?", taskId);
    }

}
