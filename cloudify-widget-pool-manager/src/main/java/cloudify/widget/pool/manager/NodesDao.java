package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.dto.PoolStatusCount;
import com.mysql.jdbc.Statement;
import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 7:09 PM
 */
public class NodesDao {

    public static final String TABLE_NAME = "nodes";
    public static final String COL_NODE_ID = "id";
    public static final String COL_POOL_ID = "pool_id";
    public static final String COL_NODE_STATUS = "node_status";
    public static final String COL_MACHINE_ID = "machine_id";
    public static final String COL_ALIAS_COUNT = "count";

    private JdbcTemplate jdbcTemplate;

    private static Logger logger = LoggerFactory.getLogger(NodesDao.class);

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        logger.info("this is datasrouce url [{}]", ((BasicDataSource) jdbcTemplate.getDataSource()).getUrl());
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean create(final NodeModel nodeModel) {

        // used to hold the auto generated key in the 'id' column
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement ps = con.prepareStatement(
                                "insert into " + TABLE_NAME + " (" + COL_POOL_ID + "," + COL_NODE_STATUS + "," + COL_MACHINE_ID + ") values (?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, nodeModel.poolId);
                        ps.setString(2, nodeModel.nodeStatus.name());
                        ps.setString(3, nodeModel.machineId);
                        return ps;
                    }
                },
                keyHolder
        );

        // keep data integrity - fetch the last insert id and update the model
        nodeModel.id = keyHolder.getKey().longValue();

        return affected > 0;
    }

    public List<PoolStatusCount> getPoolStatusCounts() {
        return jdbcTemplate.query("select count(*) as '" + COL_ALIAS_COUNT + "', " + COL_POOL_ID + "," + COL_NODE_STATUS + " from " + TABLE_NAME + " group by " + COL_POOL_ID + " , " + COL_NODE_STATUS,
                new PoolStatusCountRowMapper());
    }

    public List<PoolStatusCount> getPoolStatusCountsOfPool(String poolId) {
        return jdbcTemplate.query("select count(*) as '" + COL_ALIAS_COUNT + "', " + COL_POOL_ID + "," + COL_NODE_STATUS + " from " + TABLE_NAME + " where " + COL_POOL_ID + " = ? group by " + COL_POOL_ID + " , " + COL_NODE_STATUS,
                new Object[]{poolId},
                new PoolStatusCountRowMapper());
    }

    public List<NodeModel> readAllOfPool(String poolId) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?",
                new Object[]{poolId},
                new BeanPropertyRowMapper<NodeModel>(NodeModel.class));
    }

    public NodeModel read(long nodeId) {
        try {
            return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where " + COL_NODE_ID + " = ?",
                    new Object[]{nodeId},
                    new BeanPropertyRowMapper<NodeModel>(NodeModel.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(NodeModel nodeModel) {
        return jdbcTemplate.update(
                "update " + TABLE_NAME + " set " + COL_POOL_ID + " = ?," + COL_NODE_STATUS + " = ?," + COL_MACHINE_ID + " = ? where " + COL_NODE_ID + " = ?",
                nodeModel.poolId, nodeModel.nodeStatus.name(), nodeModel.machineId, nodeModel.id);
    }

    public int delete(long nodeId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_NODE_ID + " = ?", nodeId);
    }

    /**
     * explicit mapping is required for the {@link NodeStatus}, as enum can't be mapped
     * with the {@link BeanPropertyRowMapper}.
     */
    public static class PoolStatusCountRowMapper implements RowMapper<PoolStatusCount> {

        @Override
        public PoolStatusCount mapRow(ResultSet rs, int rowNum) throws SQLException {
            PoolStatusCount poolStatusCount = new PoolStatusCount();
            poolStatusCount.setCount(rs.getInt(COL_ALIAS_COUNT));
            poolStatusCount.setPoolId(rs.getString(COL_POOL_ID));
            poolStatusCount.setNodeStatus(NodeStatus.valueOf(rs.getString(COL_NODE_STATUS)));
            return poolStatusCount;
        }
    }

    public NodeModel occupyNode(PoolSettings poolSettings) {
        List<NodeModel> nodeModels = jdbcTemplate.queryForList("select * from " + TABLE_NAME + " where  " + COL_NODE_STATUS + " = ? ", NodeModel.class, NodeStatus.BOOTSTRAPPED);
        for (NodeModel nodeModel : nodeModels) {
            int updated = jdbcTemplate.update("update " + TABLE_NAME + " set " + COL_NODE_STATUS + " = ? where " + COL_NODE_ID + " = ? and " + COL_NODE_STATUS + " =  ? ", NodeStatus.OCCUPIED, nodeModel.id, NodeStatus.BOOTSTRAPPED);
            if (updated == 1) {
                return nodeModel;
            }
        }
        return null;
    }
}
