package cloudify.widget.hp;

import cloudify.widget.api.clouds.*;
import cloudify.widget.common.CloudExecResponseImpl;
import cloudify.widget.common.GsObjectMapper;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HostAndPort;
import com.google.inject.Module;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.openstack.nova.v2_0.config.NovaProperties;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Collections2.transform;


/**
 * User: eliranm
 * Date: 6/10/14
 * Time: 7:24 PM
 */
public class HpGrizzlyCloudServerApi implements CloudServerApi<CloudServerPojo, HpGrizzlyCloudServerCreated, HpConnectDetails, HpMachineOptions, HpGrizzlySshDetails> {


    private static Logger logger = LoggerFactory.getLogger(HpGrizzlyCloudServerApi.class);

    private static final String MACHINE_STATUS_ACTIVE = "ACTIVE";
    private static final int HTTP_NOT_FOUND = 404;
    private static final int SERVER_POLLING_INTERVAL_MILLIS = 10 * 1000; // 10 seconds

    private final XPath xpath = XPathFactory.newInstance().newXPath();

    private final Client client;

    private String endpoint;
    private String identityEndpoint;
    private String tokenSession;
    private WebResource service;

    private final DocumentBuilderFactory dbf;
    private final Object xmlFactoryMutex = new Object();

    private ContextBuilder contextBuilder;
    private ComputeServiceContext computeServiceContext;
    private ComputeService computeService;

    private HpConnectDetails connectDetails;


    public HpGrizzlyCloudServerApi() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(false);
        final ClientConfig config = new DefaultClientConfig();
        this.client = Client.create(config);
    }


    @Override
    public Collection<CloudServerPojo> listByMask(final String mask) {

        // ~!~
//        listServerIds()
//        Set<? extends NodeMetadata> nodeMetadatas = computeService.listNodesDetailsMatching(new Predicate<ComputeMetadata>() {
//            @Override
//            public boolean apply(@Nullable ComputeMetadata computeMetadata) {
//                NodeMetadata nodeMetadata = (NodeMetadata) computeMetadata;
//
//                return nodeMetadata.getStatus() == NodeMetadata.Status.RUNNING &&
//                        (mask == null || nodeMetadata.getName().contains(mask));
//            }
//        });
//
//        return transform(nodeMetadatas, new Function<NodeMetadata, HpCloudServer>() {
//            @Override
//            public HpCloudServer apply(@Nullable NodeMetadata o) {
//                return new HpCloudServer(computeService, o);
//            }
//        });

        Collection<OpenstackNode> openstackNodes = listServers(this.tokenSession);

        openstackNodes = filter( openstackNodes, new Predicate<OpenstackNode>() {
            @Override
            public boolean apply(OpenstackNode openstackNode) {
                return openstackNode.getName().startsWith(mask);
            }
        });

        return transform( openstackNodes, new Function<OpenstackNode, CloudServerPojo>() {
            @Override
            public CloudServerPojo apply(OpenstackNode openstackNode) {
                return fromOpenstackNode( openstackNode);
            }
        });
    }

    @Override
    public CloudServerPojo get(String serverId) {
        return fromOpenstackNode(getNode(serverId, this.tokenSession));
    }

    private CloudServerPojo fromOpenstackNode( OpenstackNode node ){
        CloudServerPojo result = new CloudServerPojo();

        result.setId( node.getId() );
        result.setName( node.getName() );

        ServerIp ip = new ServerIp();
        ip.publicIp = node.getPublicIp();
        ip.privateIp = node.getPrivateIp();
        result.setIp(ip);

        result.setRunning( MACHINE_STATUS_ACTIVE.equalsIgnoreCase(node.getStatus()) );
        result.setStopped( !MACHINE_STATUS_ACTIVE.equalsIgnoreCase(node.getStatus()) );
        result.setStatus( node.getStatus() );

        return result;
    }

    @Override
    public void delete(String id) {
        try {
            terminateServer(id, tokenSession, Long.MAX_VALUE);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void rebuild(String id) {
        throw new UnsupportedOperationException("rebuild is currently not supported in this implementation");
    }

    @Override
    public Collection<HpGrizzlyCloudServerCreated> create(HpMachineOptions machineOpts) {

        LinkedList<HpGrizzlyCloudServerCreated> cloudServerCreateds = new LinkedList<HpGrizzlyCloudServerCreated>();
        try {
            HpMachineDetails machineDetails = newServer(tokenSession, Long.MAX_VALUE, machineOpts);
            String machineId = machineDetails.getMachineId();
            HpGrizzlySshDetails sshDetails = new HpGrizzlySshDetails(
                    22, machineDetails.getRemoteUsername(), connectDetails.getSshPrivateKey(), machineDetails.getPublicAddress());
            HpGrizzlyCloudServerCreated serverCreated = new HpGrizzlyCloudServerCreated(machineId, sshDetails);
            cloudServerCreateds.add(serverCreated);
        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return cloudServerCreateds;
    }

    @Override
    public String createCertificate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void connect(HpConnectDetails connectDetails) {
        setConnectDetails(connectDetails);
        connect();
    }

    @Override
    public void setConnectDetails(HpConnectDetails connectDetails) {
        this.connectDetails = connectDetails;
    }

    @Override
    public void connect() {
        // connect openstack
        createAuthenticationToken();
        // connect jclouds
        computeServiceContext = computeServiceContext();
        computeService = computeServiceContext.getComputeService();
    }

    public ComputeServiceContext computeServiceContext() {

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
//        overrides.setProperty("jclouds.keystone.credential-type", "apiAccessKeyCredentials");
        overrides.setProperty(NovaProperties.AUTO_ALLOCATE_FLOATING_IPS, Boolean.FALSE.toString());

        ContextBuilder contextBuilder = ContextBuilder.newBuilder(cloudProvider)
                .apiVersion(apiVersion)
                .endpoint(this.identityEndpoint)
                .credentials(identity, secretKey)
                .overrides(overrides)
                .modules(ImmutableSet.<Module>of(new SshjSshClientModule()));

        return contextBuilder;
    }

    @Override
    public void createSecurityGroup(ISecurityGroupDetails securityGroupDetails) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CloudExecResponse runScriptOnMachine(String script, String serverIp) {
        throw new UnsupportedOperationException( "Method runScriptOnMachine(String script, String serverIp) is not supported anymore. Please use runScriptOnMachine(String script, ISshDetails sshDetails ) instead" );
    }

    @Override
    public CloudExecResponse runScriptOnMachine(String script, HpGrizzlySshDetails sshDetails) {

        String serverIp = sshDetails.getPublicIp();
        //retrieve missing ssh details
        String user = sshDetails.getUser();
        String privateKey = sshDetails.getPrivateKey();
        int port = sshDetails.getPort();

        logger.debug("Run ssh on server: {} script: {} with username: {}" , serverIp, script, user );
        SshClient.Factory factory = computeServiceContext.getUtils().getSshClientFactory();
        LoginCredentials loginCredentials = LoginCredentials.builder().user(user).privateKey(privateKey).build();
        SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIp, port),
                loginCredentials );
        ExecResponse execResponse = null;
        boolean connectionSucceeded = false;
        int attemptsCount = 0;
        try{

            // 5 retries, 60s apart (more frequent ssh attempts are blocked at machine level)
            while( !connectionSucceeded && attemptsCount < 5 ){
                try{
                    logger.info("retry " + attemptsCount);
                    Thread.sleep( 60*1000 );
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


    public void listToLog(final String token, String path) {
        logger.info("\n- - - - - - - - - - - - - - - " + path + " - - - - - - - - - - - - - - - - - -");
        String response = list(token, path);
        logger.info(response);
    }

    public String list(String token, String path) {
        return service.path(path)
                    .header("X-Auth-Token", token)
                    .accept(MediaType.APPLICATION_JSON)
                    .get(String.class);
    }


    private DocumentBuilder createDocumentBuilder() {
        synchronized (xmlFactoryMutex) {
            // Document builder is not guaranteed to be thread sage
            try {
                // Document builders are not thread safe
                return dbf.newDocumentBuilder();
            } catch (final ParserConfigurationException e) {
                throw new IllegalStateException("Failed to set up XML Parser", e);
            }
        }

    }



    private OpenstackNode getNode(final String nodeId, final String token) {

        final String response =
                service.path("servers/" + nodeId)
                        .header("X-Auth-Token", token)
                        .header("x-auth-project-id", connectDetails.getProject())
                        .accept(MediaType.APPLICATION_XML)
                        .get(String.class);

        final OpenstackNode node = new OpenstackNode();
        try {
            final DocumentBuilder documentBuilder = createDocumentBuilder();
            final Document xmlDoc = documentBuilder.parse(new InputSource(new StringReader(response)));

            node.setId(xpath.evaluate("/server/@id", xmlDoc));
            node.setStatus(xpath.evaluate("/server/@status", xmlDoc));
            node.setName(xpath.evaluate("/server/@name", xmlDoc));

            // We expect to get 2 IP addresses, public and private. Currently we get them both in an xml
            // under a private node attribute. this is expected to change.
            final NodeList addresses =
                    (NodeList) xpath.evaluate("/server/addresses/network/ip/@addr", xmlDoc, XPathConstants.NODESET);

            if (addresses.getLength() > 0) {
                node.setPrivateIp(addresses.item(0).getTextContent());
            }
            if (addresses.getLength() > 1) {
                node.setPublicIp(addresses.item(1).getTextContent());
            }

        } catch (final XPathExpressionException e) {
            logger.error(e.getMessage(), e);
        } catch (final SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

        return node;
    }

    List<OpenstackNode> listServers(final String token) {
        final List<String> ids = listServerIds(token);
        final List<OpenstackNode> nodes = new ArrayList<OpenstackNode>(ids.size());

        for (final String id : ids) {
            nodes.add(getNode(id, token));
        }

        return nodes;
    }

    private List<String> listServerIds(final String token) {

        String response = null;
        try {
            response =
                    service.path("servers")
                            .header("X-Auth-Token", token)
                            .header("x-auth-project-id", connectDetails.getProject())
                            .accept(MediaType.APPLICATION_XML)
                            .get(String.class);

            final DocumentBuilder documentBuilder = createDocumentBuilder();
            final Document xmlDoc = documentBuilder.parse(new InputSource(new StringReader(response)));

            final NodeList idNodes = (NodeList) xpath.evaluate("/servers/server/@id", xmlDoc, XPathConstants.NODESET);
            final int howmany = idNodes.getLength();
            final List<String> ids = new ArrayList<String>(howmany);
            for (int i = 0; i < howmany; i++) {
                ids.add(idNodes.item(i).getTextContent());

            }
            return ids;

        } catch (final UniformInterfaceException e) {
            final String responseEntity = e.getResponse().getEntity(String.class).toString();
            throw new RuntimeException(e + " Response entity: " + responseEntity, e);
        } catch (final SAXException e) {
            throw new RuntimeException("Failed to parse XML Response from server. Response was: " + response
                    + ", Error was: " + e.getMessage(), e);
        } catch (final XPathException e) {
            throw new RuntimeException("Failed to parse XML Response from server. Response was: " + response
                    + ", Error was: " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to send request to server. Response was: " + response
                    + ", Error was: " + e.getMessage(), e);
        }
    }

    private void terminateServerByIp(final String serverIp, final String token, final long endTime)
            throws Exception {
        final OpenstackNode node = getNodeByIp(serverIp, token);
        if (node == null) {
            throw new IllegalArgumentException("Could not find a server with IP: " + serverIp);
        }
        terminateServer(node.getId(), token, endTime);
    }

    private OpenstackNode getNodeByIp(final String serverIp, final String token) {
        final List<OpenstackNode> nodes = listServers(token);
        for (final OpenstackNode node : nodes) {
            if (node.getPrivateIp() != null && node.getPrivateIp().equalsIgnoreCase(serverIp)
                    || node.getPublicIp() != null && node.getPublicIp().equalsIgnoreCase(serverIp)) {
                return node;
            }
        }

        return null;
    }

    private void terminateServer(final String serverId, final String token, final long endTime)
            throws Exception {
        terminateServers(Arrays.asList(serverId), token, endTime);
    }

    private void terminateServers(final List<String> serverIds, final String token, final long endTime)
            throws Exception {

        // detach public ip and delete the servers
        for (final String serverId : serverIds) {
            logger.info("deleting [{}]", serverId);
            OpenstackNode node = getNode(serverId, token);
            logger.info("deleting, found node [{}]", serverId);
            if (node.getPublicIp() != null) {
                logger.info("detaching public ip" , node.getPublicIp());
                detachFloatingIP(serverId, node.getPublicIp(), token);
                logger.info("deleting public ip", node.getPublicIp());
                deleteFloatingIP(node.getPublicIp(), token);
            }

            try {
                logger.info("sending delete request [{}]", serverId);
                service.path("servers/" + serverId)
                        .header("X-Auth-Token", token)
                        .header("x-auth-project-id", connectDetails.getProject())
                        .accept(MediaType.APPLICATION_XML)
                        .delete();

            } catch (final UniformInterfaceException e) {
                if ( e.getResponse().getStatus() != HTTP_NOT_FOUND ) {
                    final String responseEntity = e.getResponse().getEntity(String.class);
                    throw new IllegalArgumentException(e + " Response entity: " + responseEntity);
                }
            }

        }
        logger.info("requests dispatched, checking machines are down");
        int successCounter = 0;

        // wait for all servers to die
        for (final String serverId : serverIds) {
            while (System.currentTimeMillis() < endTime) {
                try {
                    OpenstackNode node = this.getNode(serverId, token);
                    logger.info("found node [{}] with status [{}]", serverId, node.getStatus() );
                } catch (final UniformInterfaceException e) {
                    logger.info("machine stopped");
                    if (e.getResponse().getStatus() == HTTP_NOT_FOUND) {
                        ++successCounter;
                        break;
                    }
                    throw e;
                }
                Thread.sleep(SERVER_POLLING_INTERVAL_MILLIS);
            }

        }

        if (successCounter == serverIds.size()) {
            return;
        }

        throw new TimeoutException("Nodes " + serverIds + " did not shut down in the required time");

    }

    /**
     * Creates server. Block until complete. Returns id
     *
     * @param machineOptions the cloud template to use for this server
     * @return the server id
     */
    private HpMachineDetails newServer(final String token, final long endTime, final HpMachineOptions machineOptions)
            throws Exception {

        final String serverId = createServer(token, machineOptions);
        logger.info("server [{}] was created", serverId );
        try {
            final HpMachineDetails md = new HpMachineDetails();
            // wait until complete
            waitForServerToReachStatus(md, endTime, serverId, token, MACHINE_STATUS_ACTIVE);
            logger.info("server [{}] is now active", serverId );
            // if here, we have a node with a private and public ip.
            final OpenstackNode node = this.getNode(serverId, token);


            if (node.getPublicIp() == null) {
                String floatingIp = allocateFloatingIP(token);
                logger.info("create floating IP [{}]", floatingIp );
                try {
                    addFloatingIP(node.getId(), floatingIp, token);
                    node.setPublicIp(floatingIp);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to associate IP " + floatingIp + " to server " + node.getId(), e);
                }
            }

            md.setPublicAddress(node.getPublicIp());
            md.setMachineId(serverId);
            md.setRemoteUsername(machineOptions.getUsername());

            return md;
        } catch (final Exception e) {
            logger.warn("server: " + serverId + " failed to start up correctly. "
                    + "Shutting it down. Error was: " + e.getMessage(), e);
            try {
                terminateServer(serverId, token, endTime);
            } catch (final Exception e2) {
                logger.warn("Error while shutting down failed machine: " + serverId + ". Error was: " + e.getMessage()
                        + ".It may be leaking.", e);
            }
            throw e;
        }

    }

    private String createServer(final String token, final HpMachineOptions machineOptions)  {

        final String serverName = machineOptions.getMask() + System.currentTimeMillis();
        final String securityGroup = machineOptions.getSecurityGroup();
        final String networkUuid = machineOptions.getNetworkUuid();
        final String keyPairName = machineOptions.getKeyPairName();

        // Start the machine!
        final String json =
                "{\"server\":" +
                    "{" +
                        "\"name\":\"" + serverName + "\"," +
                        "\"imageRef\":\"" + machineOptions.getImageId() + "\"," +
                        "\"flavorRef\":\"" + machineOptions.getHardwareId() + "\"," +
                        "\"key_name\":\"" + keyPairName + "\"," +
                        "\"security_groups\":[{" +
                            "\"name\":\"" + securityGroup + "\"" +
                        "}]," +
                        "\"networks\":[{" +
                            "\"uuid\":\"" + networkUuid + "\"" +
                        "}]" +
                    "}" +
                "}";

        String serverBootResponse;
        try {
            serverBootResponse =
                    service.path("servers")
                            .header("Content-Type", "application/json")
                            .header("X-Auth-Token", token)
                            .header("x-auth-project-id", connectDetails.getProject())
                            .accept(MediaType.APPLICATION_XML)
                            .post(String.class, json);

        } catch (final UniformInterfaceException e) {
            final String responseEntity = e.getResponse().getEntity(String.class);
            throw new RuntimeException(e + " Response entity: " + responseEntity, e);
        }

        try {
            // if we are here, the machine started!
            final DocumentBuilder documentBuilder = createDocumentBuilder();
            final Document doc = documentBuilder.parse(new InputSource(new StringReader(serverBootResponse)));

            final String serverId = xpath.evaluate("/server/@id", doc);

            OpenstackNode node = this.getNode(serverId, token);

            if (!node.getStatus().startsWith("BUILD")) {
                throw new IllegalStateException("Expected server status of BUILD(*), got: " + node.getStatus());
            }

            return serverId;
        } catch (final XPathExpressionException e) {
            throw new RuntimeException("Failed to parse XML Response from server. Response was: "
                    + serverBootResponse + ", Error was: " + e.getMessage(), e);
        } catch (final SAXException e) {
            throw new RuntimeException("Failed to parse XML Response from server. Response was: "
                    + serverBootResponse + ", Error was: " + e.getMessage(), e);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to send request to server. Response was: " + serverBootResponse
                    + ", Error was: " + e.getMessage(), e);
        }
    }

    private void waitForServerToReachStatus(final HpMachineDetails md, final long endTime, final String serverId,
                                            final String token, final String status)
            throws TimeoutException, InterruptedException {

        final String respone = null;
        while (true) {

            final OpenstackNode node = this.getNode(serverId, token);

            final String currentStatus = node.getStatus().toLowerCase();

            if (currentStatus.equalsIgnoreCase(status)) {

                md.setPrivateAddress(node.getPrivateIp());
                break;
            } else {
                if (currentStatus.contains("error")) {
                    throw new RuntimeException("Server provisioning failed. Node ID: " + node.getId() + ", status: "
                            + node.getStatus());
                }

            }

            if (System.currentTimeMillis() > endTime) {
                throw new TimeoutException("timeout creating server. last status:" + respone);
            }

            Thread.sleep(SERVER_POLLING_INTERVAL_MILLIS);

        }

    }

    @SuppressWarnings("rawtypes")
    List<OpenstackFloatingIp> listFloatingIPs(final String token)
            throws SAXException, IOException {
        final String response =
                service.path("os-floating-ips")
                        .header("X-Auth-Token", token)
                        .header("x-auth-project-id", connectDetails.getProject())
                        .accept(MediaType.APPLICATION_JSON)
                        .get(String.class);

        final GsObjectMapper mapper = new GsObjectMapper();
        final Map map = mapper.readValue(new StringReader(response), Map.class);
        @SuppressWarnings("unchecked")
        final List<Map> list = (List<Map>) map.get("floating_ips");
        final List<OpenstackFloatingIp> floatingIps = new ArrayList<OpenstackFloatingIp>(map.size());

        for (final Map floatingIpMap : list) {
            final OpenstackFloatingIp ip = new OpenstackFloatingIp();

            final Object instanceId = floatingIpMap.get("instance_id");

            ip.setInstanceId(instanceId == null ? null : instanceId.toString());
            ip.setIp((String) floatingIpMap.get("ip"));
            ip.setFixedIp((String) floatingIpMap.get("fixed_ip"));
            ip.setId(floatingIpMap.get("id").toString());
            floatingIps.add(ip);
        }
        return floatingIps;

    }

    private OpenstackFloatingIp getFloatingIpByIp(final String ip, final String token)
            throws SAXException, IOException {
        final List<OpenstackFloatingIp> allips = listFloatingIPs(token);
        for (final OpenstackFloatingIp floatingIP : allips) {
            if (ip.equals(floatingIP.getIp())) {
                return floatingIP;
            }
        }

        return null;
    }

    /**
     * ******************
     * Deletes a floating IP.
     *
     * @param ip    .
     * @param token .
     * @throws SAXException .
     * @throws IOException  .
     */
    public void deleteFloatingIP(final String ip, final String token)
            throws SAXException, IOException {

        final OpenstackFloatingIp floatingIp = getFloatingIpByIp(ip, token);
        if (floatingIp == null) {
            logger.warn("Could not find floating IP " + ip + " in list. IP was not deleted.");
        } else {
            service.path("os-floating-ips/" + floatingIp.getId())
                    .header("X-Auth-Token", token)
                    .header("x-auth-project-id", connectDetails.getProject())
                    .accept(MediaType.APPLICATION_JSON)
                    .delete();

        }

    }

    /**
     * ***********
     * Allocates a floating IP.
     *
     * @param token .
     * @return .
     */
    public String allocateFloatingIP(final String token) {

        try {
            final String resp =
                    service.path("os-floating-ips")
                            .header("Content-type", "application/json")
                            .header("X-Auth-Token", token)
                            .header("x-auth-project-id", connectDetails.getProject())
                            .accept(MediaType.APPLICATION_JSON)
                            .post(String.class, "");

            final Matcher m = Pattern.compile("\"ip\": \"([^\"]*)\"").matcher(resp);
            if (m.find()) {
                return m.group(1);
            } else {
                throw new IllegalStateException("Failed to allocate floating IP - IP not found in response");
            }
        } catch (final UniformInterfaceException e) {
            logRestError(e);
            throw new IllegalStateException("Failed to allocate floating IP", e);
        }

    }

    private void logRestError(final UniformInterfaceException e) {
        logger.error("REST Error: " + e.getMessage());
        logger.error("REST Status: " + e.getResponse().getStatus());
        logger.error("REST Message: " + e.getResponse().getEntity(String.class));
    }

    /**
     * Attaches a previously allocated floating ip to a server.
     *
     * @param serverid .
     * @param ip       public ip to be assigned .
     * @param token    .
     * @throws Exception .
     */
    public void addFloatingIP(final String serverid, final String ip, final String token)
            throws Exception {

        service.path("servers/" + serverid + "/action")
                .header("Content-type", "application/json")
                .header("X-Auth-Token", token).header("x-auth-project-id", connectDetails.getProject())
                .accept(MediaType.APPLICATION_JSON)
                .post(String.class,
                        String.format("{\"addFloatingIp\":{\"server\":\"%s\",\"address\":\"%s\"}}", serverid, ip));

    }

    /**
     * *******
     * Detaches a floating IP from a server.
     *
     * @param serverId .
     * @param ip       .
     * @param token    .
     */
    public void detachFloatingIP(final String serverId, final String ip, final String token) {

        service.path("servers/" + serverId + "/action")
                .header("Content-type", "application/json")
                .header("X-Auth-Token", token).header("x-auth-project-id", connectDetails.getProject())
                .accept(MediaType.APPLICATION_JSON)
                .post(String.class,
                        String.format("{\"removeFloatingIp\":{\"server\": \"%s\", \"address\": \"%s\"}}",
                                serverId, ip));

    }


    /**
     * *******
     *
     *
     *
     * Sends a request to get all tokens
     *
     *
     * body should be :
     *
     * {
     *           "auth": {
     *               "identity": {
     *                   "methods": ["password"],
     *                   "password": {
     *                   "user": {
     *                       "name": "__username__",
     *                       "password": "__password__"
     *                   }
     *               }
     *           },
     *           "scope": {
     *               "project": {
     *                   "id": "__project__"
     *               }
     *           }
     *       }
     *    }
     *
     *
     *
     *
     * Creates an openstack keystone authentication token.
     *
     * @return the authentication token.
     */
    private String createAuthenticationToken() {

        if (tokenSession != null) {
            return tokenSession;
        }

        if (connectDetails == null) {
            throw new IllegalArgumentException("connect details must not be null");
        }

        identityEndpoint = connectDetails.getIdentityEndpoint();

        // we only use api-access-key as credential type (password credential type is also available)
        String json =  "{  \n" +
                "\"auth\": {  \n" +
                "  \"identity\": {  \n" +
                "    \"methods\": [\"password\"],  \n" +
                "    \"password\": {  \n" +
                "      \"user\": {  \n" +
                "      \"name\": \"__username__\",  \n" +
                "      \"password\": \"__password__\"  \n" +
                "      }  \n" +
                "    }  \n" +
                "},  \n" +
                "\"scope\": {  \n" +
                "    \"project\": {  \n" +
                "        \"id\": \"__project__\"  \n" +
                "      }  \n" +
                "    }  \n" +
                "  }  \n" +
                "}";

        json = json.replace("__username__", connectDetails.getKey());
        json = json.replace("__password__", connectDetails.getSecretKey());
        json = json.replace("__project__", connectDetails.getProject());

        final WebResource identityService = client.resource(this.identityEndpoint);

        // get the response
        ClientResponse response=identityService.path("tokens")
                .header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, json);

        // get the body

        final String resp =  response.getEntity(String.class);


        try {
            logger.info("reading token");

            this.tokenSession = response.getHeaders().getFirst("X-Subject-Token");

            logger.info("parsing result");
            GsObjectMapper m = new GsObjectMapper();
            Map responseMap = m.readValue(resp, Map.class);

            List<Map> l = ((List) ( (Map)responseMap.get("token")).get("catalog"));
            List<Map> computeEndpoints = null;
            for (Map catalogItem : l) {
                if ( "compute".equalsIgnoreCase((String) catalogItem.get("type"))){
                    computeEndpoints = (List<Map>) catalogItem.get("endpoints");
                }
            }

            if ( computeEndpoints == null ){
                throw new RuntimeException("unable to find compute endpoint");
            }

            String computeEndpointUrl = null;
            for (Map computeEndpoint : computeEndpoints) {
                if ( connectDetails.getRegion().equalsIgnoreCase((String)computeEndpoint.get("region")) ){
                    computeEndpointUrl = (String)computeEndpoint.get("url");
                }
            }

            if ( computeEndpointUrl == null ){
                throw new RuntimeException("did not find compute endpoint url");
            }

            this.endpoint = computeEndpointUrl;
            logger.info("found endpoint [{}]", this.endpoint);
            this.service = client.resource(this.endpoint);

            return this.tokenSession;

        } catch(Exception e){
            throw new RuntimeException("unable to get endpoint and token [ " + connectDetails + "]", e);
        }
    }

    public void list(String path) {
        listToLog(tokenSession, path);

    }
}
