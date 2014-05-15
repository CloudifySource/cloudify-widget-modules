package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.Utils;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.DecisionType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mysql.jdbc.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:50 PM
 */
public class DecisionsDao {

    private static Logger logger = LoggerFactory.getLogger(DecisionsDao.class);

    public static final String TABLE_NAME = "decisions";
    public static final String COL_ID = "id";
    public static final String COL_DECISION_TYPE = "decision_type";
    public static final String COL_POOL_ID = "pool_id";
    public static final String COL_APPROVED = "approved";
    public static final String COL_DETAILS = "details";

    public static final String COL_ALIAS_COUNT = "count";

    private JdbcTemplate jdbcTemplate;

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public boolean create(final DecisionModel decisionModel) {

        // used to hold the auto generated key in the 'id' column
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        int affected = jdbcTemplate.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                        PreparedStatement ps = con.prepareStatement(
                                "insert into " + TABLE_NAME + " (" + COL_DECISION_TYPE + "," + COL_POOL_ID + "," + COL_APPROVED + "," + COL_DETAILS + ") values (?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS // specify to populate the generated key holder
                        );
                        ps.setString(1, decisionModel.decisionType.name());
                        ps.setString(2, decisionModel.poolId);
                        ps.setBoolean(3, decisionModel.approved);
                        ps.setString(4, Utils.objectToJson(decisionModel.details));
                        return ps;
                    }
                },
                keyHolder
        );

        // keep data integrity - fetch the last insert id and update the model
        decisionModel.id = keyHolder.getKey().longValue();

        return affected > 0;
    }

    public List<DecisionModel> readAllOfPool(String poolId) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ?",
                new Object[]{poolId},
                new DecisionModelRowMapper());
    }

    public List<DecisionModel> readAllOfPoolWithDecisionType(String poolId, DecisionType decisionType) {
        return jdbcTemplate.query("select * from " + TABLE_NAME + " where " + COL_POOL_ID + " = ? and " + COL_DECISION_TYPE + " = ?",
                new Object[]{poolId, decisionType.name()},
                new DecisionModelRowMapper());
    }

    public DecisionModel read(long decisionId) {
        try {
            return jdbcTemplate.queryForObject("select * from " + TABLE_NAME + " where " + COL_ID + " = ?",
                    new Object[]{decisionId},
                    new DecisionModelRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public int update(DecisionModel decisionModel) {
        return jdbcTemplate.update(
                "update " + TABLE_NAME + " set " + COL_DECISION_TYPE + " = ?," + COL_POOL_ID + " = ?," + COL_APPROVED + " = ?," + COL_DETAILS + " = ? where " + COL_ID + " = ?",
                decisionModel.decisionType.name(), decisionModel.poolId, decisionModel.approved, Utils.objectToJson(decisionModel.details), decisionModel.id);
    }

    public int delete(long decisionId) {
        return jdbcTemplate.update("delete from " + TABLE_NAME + " where " + COL_ID + " = ?", decisionId);
    }


    public static class DecisionModelRowMapper extends BeanPropertyRowMapper<DecisionModel> {

        public DecisionModelRowMapper() {
            super(DecisionModel.class);
        }

        @Override
        protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
            Class<?> propertyType = pd.getPropertyType();
            if (DecisionDetails.class.isAssignableFrom(propertyType)) {
                ObjectMapper objectMapper = new ObjectMapper();
                String decisionDetailsString = rs.getString(index);
                try {
                    return objectMapper.readValue(decisionDetailsString, DecisionDetails.class);
                } catch (IOException e) {
                    logger.error("unable to deserialize decision details [" + decisionDetailsString + "]", e);
                }
            }
            return super.getColumnValue(rs, index, pd);
        }
    }

}
