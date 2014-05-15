package cloudify.widget.ec2;

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
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(locations = {"classpath:ec2-context.xml"})
public class Ec2OperationsTest {

    private static Logger logger = LoggerFactory.getLogger(Ec2OperationsTest.class);

    private final String echoString = "hello world";

    @Autowired
    private CloudServerApi cloudServerApi;

    @Autowired
    private IConnectDetails connectDetails;

    @Autowired
    private Ec2MachineOptions machineOptions;

    @Autowired
    public WaitTimeout waitMachineIsRunningTimeout;

    @Autowired
    public WaitTimeout waitMachineIsNotRunning;

    @Test
    public void testEc2Driver() {

        logger.info("Start test, before connect");

        cloudServerApi.connect( connectDetails );

        logger.info("Start test create ec2 machine");

        Collection<? extends CloudServerCreated> cloudServerCreatedCollection = cloudServerApi.create( machineOptions );
        logger.info("ec2CloudServerApi created");
        logger.info( "machine(s) created, count=" + cloudServerCreatedCollection.size() );
        Assert.assertEquals("should create number of machines specified", machineOptions.getMachinesCount(), CollectionUtils.size(cloudServerCreatedCollection));

        logger.info("Start test create ec2 machine, completed");

        Collection<CloudServer> machinesWithTag = cloudServerApi.listByMask("testtag2");
        Assert.assertEquals( "should list machines that were created", machineOptions.getMachinesCount(), CollectionUtils.size(machinesWithTag));
        logger.info("machines returned, size is [{}]", machinesWithTag.size());
        for (CloudServer cloudServer : machinesWithTag) {
            logger.info("cloud server name [{}]", cloudServer.getName());
        }

        /** get machine by id **/
        machinesWithTag = cloudServerApi.listByMask("testtag1");
        Assert.assertEquals( "should list machines that were created", machineOptions.getMachinesCount(), CollectionUtils.size(machinesWithTag));
        for (CloudServer cloudServer : machinesWithTag) {
            logger.info("cloud server found with id [{}]", cloudServer.getId());
            CloudServer cs = cloudServerApi.get(cloudServer.getId());
            assertNotNull("expecting server not to be null", cs);
        }

        logger.info("Running script");

        /** run script on machine **/
        for (final CloudServer machine : machinesWithTag) {

            logger.info("looking for the SshDetails in the CloudServerCreated matching the CloudServer");
            CloudServerCreated created = CollectionUtils.firstBy(cloudServerCreatedCollection, new CollectionUtils.Predicate<CloudServerCreated>() {
                @Override
                public boolean evaluate(CloudServerCreated object) {
                    return object.getId().equals(machine.getId());
                }
            });
            Ec2SshDetails sshDetails = (Ec2SshDetails) created.getSshDetails();

            CloudExecResponse cloudExecResponse = cloudServerApi.runScriptOnMachine("echo " + echoString, sshDetails);
            logger.info("run Script on machine, completed, response [{}]" , cloudExecResponse );
            assertTrue( "Script must have [" + echoString + "]" , cloudExecResponse.getOutput().contains( echoString ) );
        }

        logger.info("rebuild machine");
        for (CloudServer machine : machinesWithTag) {
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

            Exception expectedException= null;
            try {
                cloudServerApi.delete(machine.getId() + "myTest");
            } catch (RuntimeException e) {
                logger.info("exception thrown:\n [{}]", e);
                expectedException = e;
            } finally {
                assertNotNull("exception should have been thrown on delete attempt failure", expectedException);
                boolean assignableFrom = Ec2CloudServerApiOperationFailureException.class.isAssignableFrom(expectedException.getClass());
                if (!assignableFrom) {
                    logger.info("exception thrown is not expected. stack trace is:\n[{}]", expectedException.getStackTrace());
                }
                assertTrue(String.format("[%s] should be assignable from exception type thrown on delete attempt failure [%s]", Ec2CloudServerApiOperationFailureException.class, expectedException.getClass()),
                        assignableFrom);
            }
        }

    }
}