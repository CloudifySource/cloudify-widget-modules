package cloudify.widget.pool.manager;

import cloudify.widget.common.DatabaseBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by sefi on 7/23/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:pool-manager-test-context.xml"})
@ActiveProfiles({"dev", "softlayer"})
public class TestClean {

    private static final String SCHEMA = "widgetpool";
    private static final String SQL_PATH = "sql";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Before
    public void init() {

        DatabaseBuilder.destroyDatabase(jdbcTemplate, SCHEMA);
        // initializing test schema
        DatabaseBuilder.buildDatabase(jdbcTemplate, SCHEMA, SQL_PATH);

    }

    @Test
    public void clean() {

    }
}
