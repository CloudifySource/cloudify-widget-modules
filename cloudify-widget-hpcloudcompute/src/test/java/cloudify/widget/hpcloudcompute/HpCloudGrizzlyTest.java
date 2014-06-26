package cloudify.widget.hpcloudcompute;

import org.junit.Test;
//import org.openstack4j.api.OSClient;
//import org.openstack4j.openstack.OSFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * User: evgenyf
 * Date: 2/18/14
 */
@ContextConfiguration(locations = {"classpath:hpcloudcompute-grizzly-context.xml"})
public class HpCloudGrizzlyTest extends HpCloudComputeOperationsTest{

    @Autowired
    public Openstack4jDetails details;

    private static Logger logger = LoggerFactory.getLogger(HpCloudGrizzlyTest.class);

    @Test
    public void tetMe(){
        logger.info("running with [{}]" , details);
//        org.openstack4j.api.OSClient os = OSFactory.builder()
//                .endpoint()
//                .credentials(details.username, details.password)
//                .tenantName(details.tenantName)
//                .authenticate();
    }

    public static class Openstack4jDetails{
        public String endpoint = "http://127.0.0.1:5000/v2.0";
        public String username;
        public String password;
        public String tenantName;

        @Override
        public String toString() {
            return "Openstack4jDetails{" +
                    "endpoint='" + endpoint + '\'' +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", tenantName='" + tenantName + '\'' +
                    '}';
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }
    }

    public Openstack4jDetails getDetails() {
        return details;
    }

    public void setDetails(Openstack4jDetails details) {
        this.details = details;
    }
}

