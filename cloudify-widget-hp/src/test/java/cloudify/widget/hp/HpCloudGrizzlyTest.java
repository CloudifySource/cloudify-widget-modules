package cloudify.widget.hp;

import cloudify.widget.api.clouds.CloudServer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;

/**
 * User: evgenyf
 * Date: 2/18/14
 */
@ContextConfiguration(locations = {"classpath:hp-grizzly-context.xml"})
public class HpCloudGrizzlyTest extends HpCloudOperationsTest {

    private static Logger logger = LoggerFactory.getLogger(HpCloudGrizzlyTest.class);
//    @Test
//    public void testDelete(){
//        cloudServerApi.connect( connectDetails );
//        CloudServer cloudServer = cloudServerApi.get("61b07083-9e40-4648-ac1c-aed272311376");
//        logger.info("[{}]", cloudServer.isStopped());
//    }

}
