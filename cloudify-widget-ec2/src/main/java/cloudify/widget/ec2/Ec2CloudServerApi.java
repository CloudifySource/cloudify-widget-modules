package cloudify.widget.ec2;

import cloudify.widget.api.clouds.*;
import cloudify.widget.common.CloudExecResponseImpl;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.*;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.logging.config.NullLoggingModule;
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
public class Ec2CloudServerApi implements CloudServerApi {

    private static Logger logger = LoggerFactory.getLogger(Ec2CloudServerApi.class);

    private ComputeService computeService;
    private Ec2ConnectDetails connectDetails;
//    private final AWSEC2Api ec2Api;

    public Ec2CloudServerApi() {
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
            public Ec2CloudServer apply(@Nullable NodeMetadata o) {
                return new Ec2CloudServer(computeService, o);
            }
        });
    }

    @Override
    public CloudServer get(String serverId) {
        CloudServer cloudServer = null;
        NodeMetadata nodeMetadata = computeService.getNodeMetadata(serverId);
        if (nodeMetadata != null) {
            cloudServer = new Ec2CloudServer( computeService, nodeMetadata );
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
                throw new Ec2CloudServerApiOperationFailureException(
                        String.format("delete operation failed for server with id [%s].", id), e);
            }
        }
    }

    @Override
    public void rebuild(String id) {
        computeService.rebootNode( id );
    }

    @Override
    public Collection<? extends CloudServerCreated> create( MachineOptions machineOpts ) {

        logger.info( "Starting to create new node(s)..." );
        long startTime = System.currentTimeMillis();

        Ec2MachineOptions ec2MachineOptions = ( Ec2MachineOptions )machineOpts;
        String name = ec2MachineOptions.getMask();
        int machinesCount = ec2MachineOptions.getMachinesCount();
        Template template = createTemplate(ec2MachineOptions);
        Set<? extends NodeMetadata> newNodes;
        try {
            newNodes = computeService.createNodesInGroup( name, machinesCount, template );
        }
        catch (org.jclouds.compute.RunNodesException e) {
            if( logger.isErrorEnabled() ){
                logger.error( "Create EC2 node failed", e );
            }
            throw new RuntimeException( e );
        }

        long endTime = System.currentTimeMillis();
        long totalTimeSec = ( endTime - startTime )/1000;
        logger.info( "After create new node, creating took [" + ( totalTimeSec ) + "] sec." );

        List<CloudServerCreated> newNodesList = new ArrayList<CloudServerCreated>( newNodes.size() );
        for( NodeMetadata newNode : newNodes ){
            newNodesList.add( new Ec2CloudServerCreated( newNode ) );
        }

        return newNodesList;
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
        if (!(connectDetails instanceof Ec2ConnectDetails)) {
            throw new RuntimeException("expected Ec2ConnectDetails implementation");
        }
        this.connectDetails = (Ec2ConnectDetails) connectDetails;

    }

    @Override
    public void connect() {
        computeService = computeServiceContext(connectDetails).getComputeService();
    }

    public static ComputeServiceContext computeServiceContext(Ec2ConnectDetails connectDetails) {

        String accessId = connectDetails.getAccessId();
        String secretAccessKey = connectDetails.getSecretAccessKey();

        logger.info("creating compute service context");

        String cloudProvider = CloudProvider.AWS_EC2.label;
        logger.info("building new context for provider [{}]", cloudProvider);

        ContextBuilder contextBuilder = ContextBuilder.newBuilder(cloudProvider)
                .credentials(accessId, secretAccessKey);
        ComputeServiceContext context = contextBuilder.buildView(ComputeServiceContext.class);

        return context;
    }

    private Template createTemplate( Ec2MachineOptions machineOptions ) {
        TemplateBuilder templateBuilder = computeService.templateBuilder();

        String hardwareId = machineOptions.getHardwareId();
        String locationId = machineOptions.getLocationId();
        String imageId = machineOptions.getImageId();

        if( !StringUtils.isEmpty(hardwareId)){
            templateBuilder.hardwareId(hardwareId);
        }
        if( !StringUtils.isEmpty( locationId ) ){
            templateBuilder.locationId(locationId);
        }
        if( !StringUtils.isEmpty( imageId ) ){
            templateBuilder.imageId(imageId);
        }

        logger.info( "Before building template" );
        long startTime = System.currentTimeMillis();
        Template template = templateBuilder.build();
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        logger.info( "After building template, build took [" + ( totalTime ) + "] msec." );

        // we use tags to identify the node by mask
        if( machineOptions.getMask() != null ){
            template.getOptions().tags(Arrays.asList(machineOptions.getMask()));
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

    @Override
    public CloudExecResponse runScriptOnMachine(String script, ISshDetails sshDetails) {

        Ec2SshDetails ec2SshDetails = ( Ec2SshDetails )sshDetails;
        //retrieve missing ssh details
        String user = ec2SshDetails.getUser();
        String privateKey = ec2SshDetails.getPrivateKey();
        int port = ec2SshDetails.getPort();
        String serverIp = ec2SshDetails.getPublicIp();

        logger.debug("Run ssh on server: {} script: {}" , serverIp, script );
        Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
        SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
        LoginCredentials loginCredentials = LoginCredentials.builder().user(user).privateKey(privateKey).build();

        SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIp, port),
                loginCredentials );
        ExecResponse execResponse = null;
        try{
            sshConnection.connect();
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
