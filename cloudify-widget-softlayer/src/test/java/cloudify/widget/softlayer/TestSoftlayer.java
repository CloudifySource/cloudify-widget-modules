package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.CloudExecResponse;
import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.CloudServerCreated;
import cloudify.widget.common.CloudExecResponseImpl;
import com.google.common.collect.Iterables;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ComputeMetadata;
import org.jclouds.compute.domain.Image;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.OsFamily;
import org.jclouds.compute.predicates.ImagePredicates;
import org.jclouds.domain.Location;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/4/14
 * Time: 1:09 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:softlayer-context.xml"})
public class TestSoftlayer {

    private static Logger logger = LoggerFactory.getLogger(TestSoftlayer.class);
    private ComputeService computeService;
    private ComputeServiceContext context;

    @Autowired
    private SoftlayerCloudCredentials softlayerCloudCredentials;

    @Before
    public void setup() {
        logger.info("before setup...");
        context = SoftlayerCloudUtils.computeServiceContext(softlayerCloudCredentials.getUser(), softlayerCloudCredentials.getApiKey(), true);
        computeService = context.getComputeService();
        logger.info("setup finished: \n\tcontext is [{}] \n\tcompute service is [{}]", context, computeService);
    }

    @Ignore
    public void testImageId() {
        Image first = Iterables.get(computeService.listImages(), 0);
        assert ImagePredicates.idEquals(first.getId()).apply(first);
        Image second = Iterables.get(computeService.listImages(), 1);
        assert !ImagePredicates.idEquals(first.getId()).apply(second);
    }

    @Test
    public void testContext() {
        logger.info("testing context [{}]", context);
        assertNotNull("context is null!", context);
    }

    @Test
    public void testComputeService() {
        logger.info("testing compute service [{}]", computeService);
        assertNotNull("compute service is null!", computeService);
    }

    @Test
    public void testGetAllMachinesWithTag() {

//        SoftLayerClient softlayerClient = ContextBuilder.newBuilder("softlayer").buildApi(SoftLayerClient.class);

        SoftlayerCloudServerApi softlayerCloudServerApi = new SoftlayerCloudServerApi(computeService, null);
        Collection<CloudServer> machinesWithTag = softlayerCloudServerApi.getAllMachinesWithTag("");
//        assertThat("this string", is("this string"));
        logger.info("machines returned, size is [{}]", machinesWithTag.size());
        for (CloudServer cloudServer : machinesWithTag) {
            logger.info("cloud server provider ip is [{}]", cloudServer.getServerIp().privateIp);
        }
    }

    @Test
    public void testCreateMachine() {

//        SoftLayerClient softlayerClient = ContextBuilder.newBuilder("softlayer").buildApi(SoftLayerClient.class);
        logger.info("Start test create softlayer machine");
        SoftlayerMachineOptions machineOptions = new SoftlayerMachineOptions( "testsoft" );
        machineOptions.hardwareId( "1640,2238,13899" ).locationId( "37473" ).osFamily(OsFamily.CENTOS);
        logger.info("machine options created");
        SoftlayerCloudServerApi softlayerCloudServerApi = new SoftlayerCloudServerApi(computeService, null);
        logger.info("softlayerCloudServerApi created");
        Collection<CloudServerCreated> cloudServerCreatedCollection = softlayerCloudServerApi.create( machineOptions );
        logger.info( "machine(s) created, count=" + cloudServerCreatedCollection.size() );
        int i = 0;
        for( CloudServerCreated cloudServerCreated : cloudServerCreatedCollection ){
            logger.info( "machine created, [{}] ", i++, ( ( SoftlayerCloudServerCreated )cloudServerCreated ).getNewNode() );
        }

        logger.info("Start test create softlayer machine, completed");
    }

    @Test
    public void runScriptOnNodeTest(){

        final String echoString = "hello world";

        Set<? extends ComputeMetadata> computeMetadatas = computeService.listNodes();
        for( ComputeMetadata computeMetadata : computeMetadatas ){
            NodeMetadata nodeMetadata = ( NodeMetadata )computeMetadata;
            Set<String> publicAddresses = nodeMetadata.getPublicAddresses();
              if( !publicAddresses.isEmpty() ){
                  SoftlayerCloudServerApi softlayerCloudServerApi = new SoftlayerCloudServerApi(computeService, null);
                  String publicAddress = publicAddresses.iterator().next();
                  CloudExecResponseImpl cloudExecResponse =
                          ( CloudExecResponseImpl )softlayerCloudServerApi.runScriptOnMachine("echo " + echoString, publicAddress, null);
                  logger.info("run Script on machine, completed, response [{}]" , cloudExecResponse );
                  assertTrue( "Script must have [" + echoString + "]" , cloudExecResponse.getOutput().contains( echoString ) );
                  break;
            }
        }
    }
}