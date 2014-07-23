package cloudify.widget.website;

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
@ContextConfiguration(locations = {"classpath:context.xml"})
public class TestClean {

    private static final String SCHEMA = "widgetwebsite";
    private static final String SQL_PATH = "schema";

    @Autowired
    private JdbcTemplate websiteJdbcTemplate;

    @Before
    public void init() {

        DatabaseBuilder.destroyDatabase(websiteJdbcTemplate, SCHEMA);
        // initializing test schema
        DatabaseBuilder.buildDatabase(websiteJdbcTemplate, SCHEMA, SQL_PATH);

    }

    @Test
    public void clean() {

    }
}
