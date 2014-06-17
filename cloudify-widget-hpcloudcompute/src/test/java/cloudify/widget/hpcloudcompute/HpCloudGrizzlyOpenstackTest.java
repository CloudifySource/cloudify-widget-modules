package cloudify.widget.hpcloudcompute;

import cloudify.widget.common.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

/**
 * User: eliranm
 * Date: 6/9/14
 * Time: 2:03 PM
 */
@ContextConfiguration(locations = {"classpath:hpcloudcompute-grizzly-context.xml"})
@RunWith(SpringJUnit4ClassRunner.class)
public class HpCloudGrizzlyOpenstackTest {

    private static Logger logger = LoggerFactory.getLogger(HpCloudGrizzlyOpenstackTest.class);


    @Autowired
    private HpCloudComputeConnectDetails connectDetails;

    @Autowired
    private HpCloudComputeMachineOptions machineOptions;

    @Autowired
    private HpGrizzlyCloudServerApi hpGrizzlyCloudServerApi;


    @Test
    public void testGrizzly() {

        logger.info("connecting to hp grizzly");
        hpGrizzlyCloudServerApi.connect(connectDetails);

        logger.info("creating server with options [{}]", machineOptions);
        Collection<HpGrizzlyCloudServerCreated> serverCreateds = hpGrizzlyCloudServerApi.create(machineOptions);

        logger.info("server created, listing servers with mask [{}]", machineOptions.getMask());
        hpGrizzlyCloudServerApi.listByMask(machineOptions.getMask());

        String serverId = CollectionUtils.first(serverCreateds).getId();
        logger.info("deleting server with id [{}]", serverId);
        hpGrizzlyCloudServerApi.delete(serverId);

        logger.info("server deleted, listing servers with mask [{}]", machineOptions.getMask());
        hpGrizzlyCloudServerApi.listByMask(machineOptions.getMask());

    }

    @Test
    public void testConnect() {

        logger.info("connecting to hp grizzly");
        hpGrizzlyCloudServerApi.connect(connectDetails);

    }

    @Test
    public void testCreate() {

        logger.info("connecting to hp grizzly");
        hpGrizzlyCloudServerApi.connect(connectDetails);

        logger.info("creating server with options [{}]", machineOptions);
        Collection<HpGrizzlyCloudServerCreated> serverCreateds = hpGrizzlyCloudServerApi.create(machineOptions);

        logger.info("server created, listing servers with mask [{}]", machineOptions.getMask());
        hpGrizzlyCloudServerApi.listByMask(machineOptions.getMask());

    }

    @Test
    public void testList() {

        logger.info("connecting to hp grizzly");
        hpGrizzlyCloudServerApi.connect(connectDetails);

        hpGrizzlyCloudServerApi.list("images");

//        logger.info("server created, listing servers with mask [{}]", machineOptions.getMask());
//        hpGrizzlyCloudServerApi.listByMask(machineOptions.getMask());

    }

    @Test
    public void testDelete() {

        logger.info("connecting to hp grizzly");
        hpGrizzlyCloudServerApi.connect(connectDetails);

        String serverId = "4101945";

        logger.info("deleting server with id [{}]", serverId);
        hpGrizzlyCloudServerApi.delete(serverId);

        logger.info("server deleted, listing servers with mask [{}]", machineOptions.getMask());
        hpGrizzlyCloudServerApi.listByMask(machineOptions.getMask());

    }



}















