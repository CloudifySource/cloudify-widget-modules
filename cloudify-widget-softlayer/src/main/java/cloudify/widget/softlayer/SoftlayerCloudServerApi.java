package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.CloudExecResponse;
import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.api.clouds.ISecurityGroupDetails;
import cloudify.widget.common.CloudExecResponseImpl;
import com.google.common.net.HostAndPort;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.mashape.unirest.http.JsonNode;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.domain.ExecResponse;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.domain.LoginCredentials;
import org.jclouds.logging.config.NullLoggingModule;
import org.jclouds.ssh.SshClient;
import org.jclouds.sshj.config.SshjSshClientModule;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: eliranm
 * Date: 2/4/14
 * Time: 3:41 PM
 */
public class SoftlayerCloudServerApi implements CloudServerApi<SoftlayerCloudServer, SoftlayerCloudServerCreated, SoftlayerConnectDetails, SoftlayerMachineOptions, SoftlayerSshDetails> {

    private static Logger logger = LoggerFactory.getLogger(SoftlayerCloudServerApi.class);

    @Autowired
    private SoftlayerRestApi softlayerRestApi;

    private SoftlayerConnectDetails connectDetails;

    private boolean useCommandLineSsh;

    public SoftlayerCloudServerApi() {
    }


    @Override
    public void connect(SoftlayerConnectDetails connectDetails) {
        setConnectDetails(connectDetails);
        connect();
    }

    @Override
    public Collection<SoftlayerCloudServer> listByMask(final String mask) {
        ArrayList<SoftlayerCloudServer> softlayerCloudServers = new ArrayList<SoftlayerCloudServer>();
        ArrayList<JSONObject> nodeMetadatas;

        logger.info("getting all machines matching mask [{}]", mask);
        try {
            nodeMetadatas = softlayerRestApi.listByMask(mask, connectDetails);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to retrieve list of nodes.", e);
            }
            throw new RuntimeException(e);
        }

        for (int i = 0; i < nodeMetadatas.size(); i++) {
            JSONObject nodeMetadata = nodeMetadatas.get(i);
            softlayerCloudServers.add(new SoftlayerCloudServer(nodeMetadata));
        }

        return softlayerCloudServers;
    }

    @Override
    public SoftlayerCloudServer get(String serverId) {
        JSONObject node;

        try {
            node = softlayerRestApi.getNode(serverId, connectDetails);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Failed to retrieve list of nodes.", e);
            }
            throw new RuntimeException(e);
        }

        return new SoftlayerCloudServer(node);
    }

    @Override
    public void delete(String id) {
        if (logger.isDebugEnabled()) {
            logger.debug("calling destroyNode, id is [{}]", id);
        }
        try {
            softlayerRestApi.destroyNode(id, connectDetails);
        } catch (Throwable e) {
            throw new SoftlayerCloudServerApiOperationFailureException(
                    String.format("delete operation failed for server with id [%s].", id), e);
        }
    }

    @Override
    public void rebuild(String id) {
        logger.info("rebuilding : [{}]", id);
        throw new UnsupportedOperationException("this driver does not support this operation");
    }

    @Override
    public void setConnectDetails(SoftlayerConnectDetails connectDetails) {
        this.connectDetails = connectDetails;

    }

    @Override
    public void connect() {
        // We're using REST, nothing to connect to.
    }

    @Override
    public Collection<SoftlayerCloudServerCreated> create(SoftlayerMachineOptions softlayerMachineOptions) {

        String name = softlayerMachineOptions.name();
        int machinesCount = softlayerMachineOptions.machinesCount();
        JsonNode template = softlayerRestApi.buildTemplate(softlayerMachineOptions, connectDetails);
        Set<JSONObject> newNodes;
        try {
            logger.info("creating [{}] new machine with name [{}]", machinesCount, name);
            newNodes = softlayerRestApi.createNodes(template, machinesCount, connectDetails);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error("Create softlayer node failed", e);
            }
            throw new RuntimeException(e);
        }

        List<SoftlayerCloudServerCreated> newNodesList = new ArrayList<SoftlayerCloudServerCreated>(newNodes.size());
        for (JSONObject newNode : newNodes) {
            newNodesList.add(new SoftlayerCloudServerCreated(newNode));
        }

        return newNodesList;
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

        throw new UnsupportedOperationException("Method runScriptOnMachine(String script, String serverIp) is not supported anymore. Please use runScriptOnMachine(String script, ISshDetails sshDetails ) instead");
    }

    private ExecResponse executeSsh(String script, SoftlayerSshDetails softlayerSshDetails) {
        ExecResponse execResponse;
        Injector i = Guice.createInjector(new SshjSshClientModule(), new NullLoggingModule());
        SshClient.Factory factory = i.getInstance(SshClient.Factory.class);
        LoginCredentials loginCredentials = LoginCredentials.builder().user(softlayerSshDetails.getUser()).password(softlayerSshDetails.getPassword()).build();
        //.privateKey(Strings2.toStringAndClose(new FileInputStream(conf.server.bootstrap.ssh.privateKey)))
        String serverIp = softlayerSshDetails.getPublicIp();
        SshClient sshConnection = factory.create(HostAndPort.fromParts(serverIp, softlayerSshDetails.getPort()),
                loginCredentials);
        try {
            sshConnection.connect();
            logger.info("ssh connected, executing");
            execResponse = sshConnection.exec(script);
            logger.info("finished execution");
        } finally {
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

    public void setUseCommandLineSsh(boolean useCommandLineSsh) {
        this.useCommandLineSsh = useCommandLineSsh;
    }

    @Override
    public CloudExecResponse runScriptOnMachine(String script, SoftlayerSshDetails softlayerSshDetails) {

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

    public SoftlayerRestApi getSoftlayerRestApi() {
        return softlayerRestApi;
    }

    public void setSoftlayerRestApi(SoftlayerRestApi softlayerRestApi) {
        this.softlayerRestApi = softlayerRestApi;
    }
}
