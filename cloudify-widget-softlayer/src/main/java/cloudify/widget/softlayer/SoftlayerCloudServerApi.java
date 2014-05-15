package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.*;
import cloudify.widget.common.CloudExecResponseImpl;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.net.HostAndPort;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.*;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.javax.annotation.Nullable;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.softlayer.compute.functions.VirtualGuestToReducedNodeMetaDataLocal;
import org.jclouds.softlayer.reference.SoftLayerConstants;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.google.common.collect.Collections2.transform;

/**
 * User: eliranm
 * Date: 2/4/14
 * Time: 3:41 PM
 */
public class SoftlayerCloudServerApi implements CloudServerApi {

    private static Logger logger = LoggerFactory.getLogger(SoftlayerCloudServerApi.class);

    private ComputeService computeService;

    private SoftlayerConnectDetails connectDetails;

    private boolean useCommandLineSsh;
    private ContextBuilder contextBuilder;


    public SoftlayerCloudServerApi(){

    }



    @Override
    public void connect(IConnectDetails connectDetails) {
       setConnectDetails( connectDetails);
        connect();
    }

    @Override
    public Collection<CloudServer> listByMask(final String mask) {
        logger.info("getting all machines matching mask [{}]", mask);
        Set<? extends NodeMetadata> nodeMetadatas = computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
            @Override
            public boolean apply(@Nullable ComputeMetadata computeMetadata) {
                return mask == null || computeMetadata.getName().startsWith(mask);
            }
        });

        return transform(nodeMetadatas, new Function<NodeMetadata, CloudServer>() {
            @Override
            public SoftlayerCloudServer apply(@Nullable NodeMetadata o) {
                return new SoftlayerCloudServer(computeService, o);
            }
        });
    }

    @Override
    public CloudServer get(String serverId) {
        CloudServer cloudServer = null;
        NodeMetadata nodeMetadata = computeService.getNodeMetadata(serverId);
        if (nodeMetadata != null) {
            cloudServer = new SoftlayerCloudServer(computeService, nodeMetadata);
        }
        return cloudServer;
    }

    @Override
    public void delete(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("calling destroyNode, id is [{}]", id);
        }
        try {
            computeService.destroyNode(id);
        } catch (Throwable e) {
            throw new SoftlayerCloudServerApiOperationFailureException(
                    String.format("delete operation failed for server with id [%s].", id), e);
        }
    }

    @Override
    public void rebuild( String id ) {
        logger.info("rebuilding : [{}]", id);
        throw new UnsupportedOperationException("this driver does not support this operation");
    }

    @Override
    public void setConnectDetails(IConnectDetails connectDetails) {
        if (!( connectDetails instanceof SoftlayerConnectDetails )){
            throw new RuntimeException("expected SoftlayerConnectDetails implementation");
        }
        this.connectDetails = (SoftlayerConnectDetails) connectDetails;

    }

    @Override
    public void connect() {
        try{
            logger.info("connecting");
            computeService = computeServiceContext( connectDetails ).getComputeService();
            if ( computeService == null ){
                throw new RuntimeException("illegal credentials");
            }
        }catch(RuntimeException e){
            logger.error("unable to connect softlayer context",e);
            throw e;
        }
    }

    private ComputeServiceContext computeServiceContext( SoftlayerConnectDetails connectDetails) {

        logger.info("creating compute service context");
        Set<Module> modules = new HashSet<Module>();

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {
                bind(org.jclouds.softlayer.compute.functions.VirtualGuestToNodeMetadata.class).to(VirtualGuestToReducedNodeMetaDataLocal.class);
            }
        });

        ComputeServiceContext context;
        Properties overrides = new Properties();

        // it is strange that we add a machine detail on the context, but it was less work.
        overrides.setProperty(SoftLayerConstants.PROPERTY_SOFTLAYER_VIRTUALGUEST_PORT_SPEED_FIRST_PRICE_ID, connectDetails.networkId );
        overrides.put("jclouds.timeouts.AccountClient.getActivePackages", String.valueOf(10 * 60 * 1000));
        overrides.put("jclouds.timeouts.AccountClient.getActivePackages", String.valueOf(10 * 60 * 1000));
        //if (connectDetails.isApiKey()) {
        overrides.put("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
        //}

        String cloudProvider = CloudProvider.SOFTLAYER.label;
        logger.info("building new context for provider [{}]", cloudProvider);
        contextBuilder = ContextBuilder.newBuilder(cloudProvider)
                .credentials(connectDetails.getUsername(), connectDetails.getKey())
                .overrides(overrides)
                .modules(modules);
        context = contextBuilder.buildView(ComputeServiceContext.class);

        return context;
    }

    @Override
    public Collection<? extends CloudServerCreated> create( MachineOptions machineOpts ) {

        SoftlayerMachineOptions softlayerMachineOptions = ( SoftlayerMachineOptions )machineOpts;
        String name = softlayerMachineOptions.name();
        int machinesCount = softlayerMachineOptions.machinesCount();
        Template template = createTemplate(softlayerMachineOptions);
        Set<? extends NodeMetadata> newNodes;
        try {
            logger.info("creating [{}] new machine with name [{}]", machinesCount, name);
            newNodes = computeService.createNodesInGroup( name, machinesCount, template );
        }
        catch (org.jclouds.compute.RunNodesException e) {
            if( logger.isErrorEnabled() ){
                logger.error( "Create softlayer node failed", e );
            }
            throw new RuntimeException( e );
        }

        List<CloudServerCreated> newNodesList = new ArrayList<CloudServerCreated>( newNodes.size() );
        for( NodeMetadata newNode : newNodes ){
            newNodesList.add( new SoftlayerCloudServerCreated( newNode ) );
        }

        return newNodesList;
    }

    private Template createTemplate( SoftlayerMachineOptions machineOptions ) {
        TemplateBuilder templateBuilder = computeService.templateBuilder();

        String hardwareId = machineOptions.getHardwareId();
        String locationId = machineOptions.getLocationId();
        OsFamily osFamily = machineOptions.getOsFamily();
        if( osFamily != null ){
            templateBuilder.osFamily(osFamily);
        }
        if( !StringUtils.isEmpty(hardwareId)){
            templateBuilder.hardwareId( hardwareId );
        }

        if( !StringUtils.isEmpty( locationId ) ){
            templateBuilder.locationId(locationId);
        }

        return templateBuilder.build();
    }

    @Override
    public String createCertificate() {
        throw new UnsupportedOperationException("create certificate is unsupported");
    }

    @Override
    public void createSecurityGroup(ISecurityGroupDetails securityGroupDetails) {
        throw new UnsupportedOperationException("create security group is unsupported in this implementation");
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    @Deprecated
    public CloudExecResponse runScriptOnMachine(String script, String serverIp) {

        throw new UnsupportedOperationException( "Method runScriptOnMachine(String script, String serverIp) is not supported anymore. Please use runScriptOnMachine(String script, ISshDetails sshDetails ) instead" );
    }

    private ExecResponse executeSsh(String script, SoftlayerSshDetails softlayerSshDetails) {

        ExecResponse execResponse;
        Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
        SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
        LoginCredentials loginCredentials = LoginCredentials.builder().user(softlayerSshDetails.getUser()).password(softlayerSshDetails.getPassword()).build();
        //.privateKey(Strings2.toStringAndClose(new FileInputStream(conf.server.bootstrap.ssh.privateKey)))
        String serverIp = softlayerSshDetails.getPublicIp();
        SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIp, softlayerSshDetails.getPort()),
                loginCredentials );
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
        return execResponse;
    }

    private ExecResponse executeCommandLineSsh(String script, SoftlayerSshDetails softlayerSshDetails) {

        String serverIp = softlayerSshDetails.getPublicIp();
        // create file from script content, to pass to the sshpass command
        File file = new File(FilenameUtils.normalize("tmp/commandLineSshScript"));
        try {
            FileUtils.write(file, script);
        } catch (IOException e) {
            logger.error("failed creating command line ssh script file", e);
        }

        // build sshpass command
        CommandLine cmdLine = new CommandLine("sshpass");
        cmdLine.addArguments(new String[]{
                "-p", softlayerSshDetails.getPassword(),
                "ssh",
                "-o", "StrictHostKeyChecking=no", // prevents "add key to known..." prompt
                "-l", softlayerSshDetails.getUser(),
                serverIp
        }, false);


        // create streams for the executor
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        InputStream inputStream = null;
        try {
            // an input stream is used where shell redirection will fail - we cannot simply
            // pass such command via the executor, e.g. "sort < file_list.txt".
            // commands with io redirection (<,>) will fail as the java process will break the
            // command apart and expect input/output redirection via the executor streams.
            // so we do just that. holy shit.
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.error("failed to create input stream to redirect cli ssh script input into sshpass command", e);
        }
        // redirect stream between the executor and the java process
        PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, errorStream, inputStream);

        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(streamHandler);

        int exitValue = 1;
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("executing ssh script via cli, command line is [{}]", StringUtils.join(cmdLine));
                logger.debug("\tmachine is [{}]", serverIp);
                logger.debug("\tscript is [{}]", script);
            }
            exitValue = executor.execute(cmdLine);
        } catch (IOException e) {
            logger.error("failed executing command line ssh call", e);
        }

        return new ExecResponse(outputStream.toString(), errorStream.toString(), exitValue);
    }


    private Set<? extends NodeMetadata> getNodeMetadataByIp(final String ip) {
        return computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
                @Override
                public boolean apply(ComputeMetadata computeMetadata) {
                    NodeMetadata nodeMetadata = (NodeMetadata) computeMetadata;
                    Set<String> publicAddresses = nodeMetadata.getPublicAddresses();
                    return publicAddresses.contains(ip);
                }
            });
    }

    public void setUseCommandLineSsh(boolean useCommandLineSsh) {
        this.useCommandLineSsh = useCommandLineSsh;
    }

    public CloudExecResponse runScriptOnMachine(String script, ISshDetails sshDetails ){

        SoftlayerSshDetails softlayerSshDetails = (SoftlayerSshDetails)sshDetails;
        String serverIp = softlayerSshDetails.getPublicIp();
        if (logger.isDebugEnabled()) {
            logger.debug("running ssh script on server [{}], script [{}], use-command-line [{}]", serverIp, script, useCommandLineSsh);
        }

        ExecResponse execResponse;

        if (useCommandLineSsh) {
            execResponse = executeCommandLineSsh(script, softlayerSshDetails);
        } else {
            execResponse = executeSsh(script, softlayerSshDetails);
        }

        return new CloudExecResponseImpl(execResponse);
    }
}
