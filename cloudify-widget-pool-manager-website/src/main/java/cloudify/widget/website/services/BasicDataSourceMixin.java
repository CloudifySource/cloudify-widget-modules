package cloudify.widget.website.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.dbcp.BasicDataSource;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Created by sefi on 9/7/14.
 */
abstract class BasicDataSourceMixin extends BasicDataSource {


    @Override
    @JsonIgnore
    public PrintWriter getLogWriter() throws SQLException {
        return super.getLogWriter();
    }

    @Override
    @JsonIgnore
    public Connection getConnection() throws SQLException {
        return super.getConnection();
    }

    @Override
    @JsonIgnore
    public Connection getConnection(String user, String pass) throws SQLException {
        return super.getConnection(user, pass);
    }

    @Override
    @JsonIgnore
    public Collection getConnectionInitSqls() {
        return super.getConnectionInitSqls();
    }

    @Override
    @JsonIgnore
    public int getLoginTimeout() throws SQLException {
        return super.getLoginTimeout();
    }
}
