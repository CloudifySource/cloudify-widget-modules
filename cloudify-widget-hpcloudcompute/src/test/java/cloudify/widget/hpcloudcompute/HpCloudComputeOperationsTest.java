package cloudify.widget.hpcloudcompute;

import cloudify.widget.api.clouds.*;
import cloudify.widget.common.CollectionUtils;
import cloudify.widget.common.MachineIsNotRunningCondition;
import cloudify.widget.common.MachineIsRunningCondition;
import cloudify.widget.common.WaitTimeout;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class HpCloudComputeOperationsTest {

    private static Logger logger = LoggerFactory.getLogger(HpCloudComputeOperationsTest.class);
    private final String[] TAGS = { "hpCloudTestTag1", "hpCloudTestTag2" };

    private final String echoString = "hello world";

    @Autowired
    protected CloudServerApi cloudServerApi;

    @Autowired
    protected IConnectDetails connectDetails;

    @Autowired
    protected HpCloudComputeMachineOptions machineOptions;

    @Autowired
    public WaitTimeout waitMachineIsRunningTimeout;

    @Autowired
    public WaitTimeout waitMachineIsNotRunning;

    @Autowired(required = false)
    private String sshUserName;



    @Test
    public void testHpCloudComputeDriver() {

        logger.info("Start test, before connect");

        cloudServerApi.connect( connectDetails );

        logger.info("Start test create hp cloud machine");

        Collection<? extends CloudServerCreated> cloudServerCreatedCollection = cloudServerApi.create( machineOptions );
        logger.info("hpCloudCloudServerApi created");
        logger.info( "machine(s) created, count=" + cloudServerCreatedCollection.size() );
        Assert.assertEquals("should create number of machines specified", machineOptions.getMachinesCount(), CollectionUtils.size(cloudServerCreatedCollection));

        logger.info("Start test create HP cloud machine, completed");

        Collection<CloudServer> machinesWithTag = cloudServerApi.listByMask(machineOptions.getMask());
        Assert.assertEquals( "should list machines that were created", machineOptions.getMachinesCount(), CollectionUtils.size(machinesWithTag));
        logger.info("machines returned, size is [{}]", machinesWithTag.size());
        for (CloudServer cloudServer : machinesWithTag) {
            logger.info("cloud server found with id [{}], name [{}]", cloudServer.getId(), cloudServer.getName());
            CloudServer cs = cloudServerApi.get(cloudServer.getId());
            assertNotNull("expecting server not to be null", cs);
        }

        logger.info("Running script");
        /** run script on machine **/
        for (final CloudServer machine : machinesWithTag) {
            String publicIp = machine.getServerIp().publicIp;
            Assert.assertNotNull( "Public Ip cannot be null, machine Id is [ " + machine.getId() + "]",  publicIp );

            logger.info("looking for the SshDetails in the CloudServerCreated matching the CloudServer");
            CloudServerCreated created = CollectionUtils.firstBy(cloudServerCreatedCollection, new CollectionUtils.Predicate<CloudServerCreated>() {
                @Override
                public boolean evaluate(CloudServerCreated object) {
                    return object.getId().equals(machine.getId());
                }
            });
            HpCloudComputeSshDetails sshDetails = (HpCloudComputeSshDetails) created.getSshDetails();

            //if sshUserName defined we need to overwrite it in received sshDetails

            if( sshUserName != null ){
                HpCloudComputeSshDetails hpCloudSshDetails = sshDetails;
                sshDetails = new HpCloudComputeSshDetails( hpCloudSshDetails.getPort(), sshUserName,
                        hpCloudSshDetails.getPrivateKey(), hpCloudSshDetails.getPublicIp() );
            }

            CloudExecResponse cloudExecResponse = cloudServerApi.runScriptOnMachine("echo " + echoString, sshDetails);
            logger.info("run Script on machine, completed, response [{}]" , cloudExecResponse );
            assertTrue( "Script must have [" + echoString + "]" , cloudExecResponse.getOutput().contains( echoString ) );
        }

        logger.info("rebuild machines...");
        for (CloudServer machine : machinesWithTag) {
            logger.info("rebuild machine, id [{}] ",machine.getId());
            cloudServerApi.rebuild(machine.getId());
        }

        logger.info("deleting all machines");

        for( CloudServer machine : machinesWithTag ) {
            logger.info("waiting for machine to run");
            MachineIsRunningCondition runCondition = new MachineIsRunningCondition();
            runCondition.setMachine(machine);

            waitMachineIsRunningTimeout.setCondition(runCondition);
            waitMachineIsRunningTimeout.waitFor();

            logger.info("deleting machine with id [{}]...", machine.getId());
            cloudServerApi.delete(machine.getId());

            logger.info("waiting for machine to stop");
            MachineIsNotRunningCondition notRunningCondition = new MachineIsNotRunningCondition();
            notRunningCondition.setMachine(machine);

            waitMachineIsNotRunning.setCondition( notRunningCondition );
            waitMachineIsNotRunning.waitFor();

            //in the case of HP cloud any exception is not thrown in the case of passed wrong id to destroyNode method
        }

    }
}