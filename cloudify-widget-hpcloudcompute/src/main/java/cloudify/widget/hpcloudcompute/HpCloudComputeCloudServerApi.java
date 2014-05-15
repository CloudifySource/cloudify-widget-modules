package cloudify.widget.hpcloudcompute;


import cloudify.widget.api.clouds.*;
import cloudify.widget.common.CloudExecResponseImpl;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.inject.Module;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.*;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.config.NovaProperties;
import org.jclouds.openstack.nova.v2_0.domain.FloatingIP;
import org.jclouds.openstack.nova.v2_0.extensions.FloatingIPApi;
import org.jclouds.openstack.nova.v2_0.features.ServerApi;
import org.jclouds.openstack.nova.v2_0.options.RebuildServerOptions;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.collect.Collections2.transform;

/**
 * User: evgeny
 * Date: 2/10/14
 * Time: 6:55 PM
 */
public class HpCloudComputeCloudServerApi implements CloudServerApi {

    private static Logger logger = LoggerFactory.getLogger(HpCloudComputeCloudServerApi.class);

    private ContextBuilder contextBuilder;
    private ComputeServiceContext computeServiceContext;
    private ComputeService computeService;
    private HpCloudComputeConnectDetails connectDetails;

    private static final String IMAGE_DELIMETER = "/";

    public HpCloudComputeCloudServerApi() {
    }

    @Override
    public Collection<CloudServer> listByMask(final String mask) {

        Set<? extends NodeMetadata> nodeMetadatas = computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
            @Override
            public boolean apply(@Nullable ComputeMetadata computeMetadata) {
                NodeMetadata nodeMetadata = ( NodeMetadata )computeMetadata;
                return nodeMetadata.getStatus() == NodeMetadata.Status.RUNNING &&
                        ( mask == null ? true : computeMetadata.getTags().contains( mask ));
            }
        });

        return transform(nodeMetadatas, new Function<NodeMetadata, CloudServer>() {
            @Override
            public HpCloudComputeCloudServer apply(@Nullable NodeMetadata o) {
                return new HpCloudComputeCloudServer(computeService, o);
            }
        });
    }

    @Override
    public CloudServer get(String serverId) {
        CloudServer cloudServer = null;
        NodeMetadata nodeMetadata = computeService.getNodeMetadata(serverId);
        if (nodeMetadata != null) {
            cloudServer = new HpCloudComputeCloudServer( computeService, nodeMetadata );
        }
        return cloudServer;
    }

    @Override
    public void delete(String id) {

        if (id != null) {
            try {
                computeService.destroyNode(id);
            }
            catch (Throwable e) {
                throw new HpCloudComputeServerApiOperationFailureException(
                        String.format("delete operation failed for server with id [%s].", id), e);
            }
        }
    }

    private ServerApi getApi( String zone ){
        NovaApi novaApi = contextBuilder.buildApi(NovaApi.class);
        ServerApi serverApi = novaApi.getServerApiForZone( zone );
        return serverApi;
    }

    @Override
    public void rebuild(String id) {

        HpCloudComputeCloudServer cloudServer = ( HpCloudComputeCloudServer )get(id);
        String imageId = cloudServer.getImageId();
        String zone = cloudify.widget.common.StringUtils.substringBefore(imageId, IMAGE_DELIMETER);
        String imageIdLocal = cloudify.widget.common.StringUtils.substringAfter( imageId, IMAGE_DELIMETER );

        String idLocal = StringUtils.substringAfter(id, IMAGE_DELIMETER);
        logger.info("rebuilding [{}] that had image id [{}] idLocal [{}] with zone [{}] and imageIdLocal [{}]", id, imageId, idLocal, zone, imageIdLocal);

        ServerApi serverApi = getApi(zone);

        RebuildServerOptions rebuildServerOptions = RebuildServerOptions.Builder.withImage( imageIdLocal );

        serverApi.rebuild( idLocal, rebuildServerOptions );
    }



    @Override
    public Collection<? extends CloudServerCreated> create( MachineOptions machineOpts ) {

        logger.info( "Starting to create new node(s)..." );
        long startTime = System.currentTimeMillis();

        HpCloudComputeMachineOptions hpCloudMachineOptions = ( HpCloudComputeMachineOptions )machineOpts;
        String name = hpCloudMachineOptions.getMask();
        int machinesCount = hpCloudMachineOptions.getMachinesCount();
        Template template = createTemplate(hpCloudMachineOptions);
        Set<? extends NodeMetadata> newNodes = null;
        try {
            newNodes = computeService.createNodesInGroup( name, machinesCount, template );
        }
        catch( Throwable e) {
            if( logger.isErrorEnabled() ){
                if( newNodes != null ){
                    logger.info( "Create Hp cloud node failed, newNodes: [{}] ", Arrays.toString( newNodes.toArray() ) );
                }
                else{
                    logger.info( "newNodes: [{}] ", newNodes );
                }
                logger.error( "Create Hp cloud node failed", e );
            }
            throw new RuntimeException( e );
        }

        long endTime = System.currentTimeMillis();
        long totalTimeSec = ( endTime - startTime )/1000;
        logger.info( "After create new node, creating took [" + ( totalTimeSec ) + "] sec." );

        String apiVersion = connectDetails.getApiVersion();
        float apiVersionNum = Float.parseFloat( apiVersion );
        logger.info( "Using apiVersionNum is [{}]", apiVersionNum );
        if( apiVersionNum >= 2 ){
            logger.info( "Handling floating IP since using api version [{}]", apiVersionNum );
            for( NodeMetadata nodeMetadata : newNodes ){
                associateFloatingIp( nodeMetadata );
            }
        }

        List<CloudServerCreated> newNodesList = new ArrayList<CloudServerCreated>( newNodes.size() );
        for( NodeMetadata newNode : newNodes ){
            newNodesList.add( new HpCloudComputeCloudServerCreated( newNode ) );
        }

        logger.info("[{}] new node(s) created [{}]", newNodesList.size(), newNodesList);
        return newNodesList;
    }

    private void associateFloatingIp( NodeMetadata nodeMetadata ) {

        String zone = cloudify.widget.common.StringUtils.substringBefore( nodeMetadata.getImageId(), IMAGE_DELIMETER );
        String id = cloudify.widget.common.StringUtils.substringAfter( nodeMetadata.getId(), IMAGE_DELIMETER );

        logger.info( "associating IP for id [{}} and zone [{}]", id, zone );
        NovaApi novaApi = contextBuilder.buildApi(NovaApi.class);

        Optional<? extends FloatingIPApi> floatingIPExtensionForZone = novaApi.getFloatingIPExtensionForZone( zone );
        FloatingIPApi floatingIPApi =  floatingIPExtensionForZone.get();

        logger.info( "floatingIPExtensionForZone for zone [{}} is [{}]", zone, floatingIPExtensionForZone );
        FluentIterable<? extends FloatingIP> floatingIPs = floatingIPApi.list();

        if( floatingIPs.isEmpty() ){
            throw new RuntimeException( "There are no available floating IPs for id [" + id +
                                        "] and zone [" + zone + "]. Unable to continue." );
        }

        logger.info( "floating IP addresses: ", Arrays.toString(floatingIPs.toList().toArray()) );

        Optional<? extends FloatingIP> firstOptionalFloatingIp = floatingIPs.first();
        FloatingIP firstFloatingIP = firstOptionalFloatingIp.get();


        firstFloatingIP = floatingIPApi.get(firstFloatingIP.getId());
        String address = firstFloatingIP.getIp();

        logger.info( "FloatingIP, add IP to server id [{}], address [{}], firstFloatingIP [{}]", id, address, firstFloatingIP );
        //add floating IP tp specific instance/node
        floatingIPApi.addToServer( address, id );
    }

    @Override
    public String createCertificate() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void connect(IConnectDetails connectDetails) {
        setConnectDetails(connectDetails);
        connect();
    }

    @Override
    public void setConnectDetails(IConnectDetails connectDetails) {
        logger.info("connecting");
        if (!(connectDetails instanceof HpCloudComputeConnectDetails)) {
            throw new RuntimeException("expected HpCloudComputeConnectDetails implementation");
        }
        this.connectDetails = (HpCloudComputeConnectDetails) connectDetails;

    }

    @Override
    public void connect() {
        computeServiceContext = computeServiceContext(connectDetails);
        computeService = computeServiceContext.getComputeService();
    }

    public ComputeServiceContext computeServiceContext(HpCloudComputeConnectDetails connectDetails) {

        contextBuilder = createContextBuilder();
        ComputeServiceContext context = contextBuilder.buildView(ComputeServiceContext.class);

        return context;
    }

    private ContextBuilder createContextBuilder(){

        String project = connectDetails.getProject();
        String key = connectDetails.getKey();
        String secretKey = connectDetails.getSecretKey();
        String identity = project + ":" + key;
        String apiVersion = connectDetails.getApiVersion();

        logger.info("creating compute service context, using [{}] apiVersion, identity is [{}]", apiVersion, identity );

        String cloudProvider = CloudProvider.HP.label;
        logger.info("building new context for provider [{}]", cloudProvider);

        Properties overrides = new Properties();
        overrides.setProperty("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
        overrides.setProperty(NovaProperties.AUTO_ALLOCATE_FLOATING_IPS, Boolean.FALSE.toString());

        ContextBuilder contextBuilder = ContextBuilder.newBuilder(cloudProvider)
                .apiVersion(apiVersion)
                .credentials(identity, secretKey)
                .overrides(overrides)
                .modules(ImmutableSet.<Module>of(new SshjSshClientModule()));

        return contextBuilder;
    }

    private Template createTemplate( HpCloudComputeMachineOptions machineOptions ) {
        TemplateBuilder templateBuilder = computeService.templateBuilder();
 /*
        Set<? extends Image> images = computeService.listImages();
        Set<? extends Hardware> hardwares = computeService.listHardwareProfiles();
        Set<? extends ComputeMetadata> listNodes = computeService.listNodes();
        Set<? extends Location> locations = computeService.listAssignableLocations();

        logger.info( "images [{}]", Arrays.toString( images.toArray() ) );
        logger.info( "hardwares [{}]", Arrays.toString( hardwares.toArray() ) );
        logger.info( "listNodes [{}]", Arrays.toString( listNodes.toArray() ) );
        logger.info( "locations [{}]", Arrays.toString( locations.toArray() ) );
*/
        String hardwareId = machineOptions.getHardwareId();
        String imageId = machineOptions.getImageId();

        if( !StringUtils.isEmpty(hardwareId)){
            templateBuilder.hardwareId(hardwareId);
        }

        if( !StringUtils.isEmpty( imageId ) ){
            templateBuilder.imageId(imageId);
        }

        if ( machineOptions.getSecurityGroup() != null ) {
            logger.info("setting security group " + machineOptions.getSecurityGroup() );
            NovaTemplateOptions options = new NovaTemplateOptions();
            options.securityGroupNames(machineOptions.getSecurityGroup());
            templateBuilder.options(options);
        }

        logger.info("Before building template");
        long startTime = System.currentTimeMillis();
        Template template = templateBuilder.build();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info( "After building template, build took [" + ( totalTime ) + "] msec." );

        // we use tags to identify the node by mask
        if( machineOptions.getMask() != null ){
            template.getOptions().tags( Arrays.asList(machineOptions.getMask()) );
        }

        return template;
    }

    @Override
    public void createSecurityGroup(ISecurityGroupDetails securityGroupDetails) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    @Deprecated
    public CloudExecResponse runScriptOnMachine(String script, String serverIp) {

        throw new UnsupportedOperationException( "Method runScriptOnMachine(String script, String serverIp) is not supported anymore. Please use runScriptOnMachine(String script, ISshDetails sshDetails ) instead" );
    }

    public CloudExecResponse runScriptOnMachine(String script, ISshDetails sshDetails ){

        HpCloudComputeSshDetails hpCloudSshDetails = ( HpCloudComputeSshDetails )sshDetails;
        String serverIp = hpCloudSshDetails.getPublicIp();
        //retrieve missing ssh details
        String user = hpCloudSshDetails.getUser();//"debian"
        String privateKey = hpCloudSshDetails.getPrivateKey();
        int port = hpCloudSshDetails.getPort();

        logger.debug("Run ssh on server: {} script: {}" , serverIp, script );
        SshClient.Factory factory = computeServiceContext.getUtils().getSshClientFactory();
        LoginCredentials loginCredentials = LoginCredentials.builder().user(user).privateKey(privateKey).build();
        SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIp, port),
                loginCredentials );
        ExecResponse execResponse = null;
        boolean connectionSucceeded = false;
        int attemptsCount = 0;
        try{
            while( !connectionSucceeded && attemptsCount < 10 ){
                try{
                    Thread.sleep( 1*1000 );
                    sshConnection.connect();
                    connectionSucceeded = true;
                }
                catch( Exception e ){
                    attemptsCount++;
                    logger.info( "failed to ssh connect, going to sleep..." );
                }
            }
            if( !connectionSucceeded ){
                throw new RuntimeException( "SSH connect failed" );
            }
            logger.info("ssh connected, executing");
            execResponse = sshConnection.exec(script);
            logger.info("finished execution");
        }
        finally{
            if (sshConnection != null)
                sshConnection.disconnect();
        }

        return new CloudExecResponseImpl( execResponse );
    }
}
