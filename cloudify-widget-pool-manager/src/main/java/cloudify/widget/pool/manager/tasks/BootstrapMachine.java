package cloudify.widget.pool.manager.tasks;

import cloudify.widget.api.clouds.CloudExecResponse;
import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.pool.manager.BootstrapScriptLoader;
import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.BootstrapProperties;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.PoolSettings;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * User: eliranm
 * Date: 3/5/14
 * Time: 6:00 PM
 */
public class BootstrapMachine extends AbstractPoolTask<BootstrapMachineConfig, Void> {

    private static Logger logger = LoggerFactory.getLogger(BootstrapMachine.class);

    @Autowired
    private NodesDao nodesDao;

    @Autowired
    private BootstrapScriptLoader bootstrapScriptLoader;

    @Override
    public Void call() throws Exception {

        NodeModel nodeModel = taskConfig.getNodeModel();
        if (nodeModel.nodeStatus == NodeStatus.BOOTSTRAPPED ||
                nodeModel.nodeStatus == NodeStatus.BOOTSTRAPPING) {
            String message = String.format(
                    "node with id [%s] is bootstrapping or is already bootstrapped, aborting bootstrap task", nodeModel.id);
            logger.info(message);
            throw new RuntimeException(message);
        }

        updateNodeModelStatus(NodeStatus.BOOTSTRAPPING);
        String script = getBootstrapScript();

        script = injectBootstrapProperties(script);

        CloudServerApi cloudServerApi = CloudServerApiFactory.create(poolSettings.getProvider().getName());
        cloudServerApi.connect(poolSettings.getProvider().getConnectDetails());
        ISshDetails sshDetails = nodeModel.machineSshDetails;

        runBootstrapScriptOnMachine(script, cloudServerApi, sshDetails);

        return null;
    }

    private String getBootstrapScript() {
        String script = poolSettings.getBootstrapProperties().getScript();

        if (script == null || script.replaceAll("\\s+","").equals("")) {
            script = bootstrapScriptLoader.readScriptFromFile();
        }

        return script;
    }

    private String injectBootstrapProperties(String script) {
        BootstrapProperties bootstrapProperties = poolSettings.getBootstrapProperties();
        return script
                .replaceAll("##publicip##", bootstrapProperties.getPublicIp())
                .replaceAll("##randomValue##", bootstrapProperties.getRandomPasswordRegex())
                .replaceAll("##privateip##", bootstrapProperties.getPrivateIp())
                .replaceAll("##cloudifyUrl##", bootstrapProperties.getCloudifyUrl())
                .replaceAll("##prebootstrapScript##", bootstrapProperties.getPreBootstrapScript())
                .replaceAll("##recipeRelativePath##", bootstrapProperties.getRecipeRelativePath())
                .replaceAll("##recipeUrl##", bootstrapProperties.getRecipeUrl());
    }

    private void expireNode( String message ) {
        updateNodeModelStatus(NodeStatus.EXPIRED); // this node is out of service - it's nominated for removal
        logger.error(message);
        throw new RuntimeException(message);
    }

    private void expireNode( Exception e ){
        updateNodeModelStatus(NodeStatus.EXPIRED); // this node is out of service - it's nominated for removal
        logger.error(e.getMessage());
        throw new RuntimeException("bootstrap got exception, expiring node",e);
    }

    private void runBootstrapScriptOnMachine(String script, CloudServerApi cloudServerApi, ISshDetails sshDetails) {

        try {
            CloudExecResponse cloudExecResponse = cloudServerApi.runScriptOnMachine(script, sshDetails);
            int exitStatus = cloudExecResponse.getExitStatus();
            String output = cloudExecResponse.getOutput();
            logger.info("finished running bootstrap on node [{}], exit status is [{}]", taskConfig.getNodeModel().id, exitStatus);
            logger.info("- - - bootstrap script execution output - - - \n{}", output);
            if (exitStatus != 0 ){
                expireNode("bootstrap failed. exist Status was " + exitStatus);
            }else if (!output.contains(taskConfig.getBootstrapSuccessText())) {
                expireNode("bootstrap script does not contain [" + taskConfig.getBootstrapSuccessText() + "]. assuming bootstrap failed.");
            } else { // success
                updateNodeModelStatus(NodeStatus.BOOTSTRAPPED);

            }
        } catch (Exception e) {
            expireNode(e);
        }
    }

    private void updateNodeModelStatus(NodeStatus nodeStatus) {
        logger.debug("bootstrap was run on the machine, updating node status to [{}]", nodeStatus);
        NodeModel updatedNodeModel = nodesDao.read(taskConfig.getNodeModel().id);
        updatedNodeModel.setNodeStatus(nodeStatus);
        nodesDao.update(updatedNodeModel);
    }


    @Override
    public TaskName getTaskName() {
        return TaskName.BOOTSTRAP_MACHINE;
    }

    @Override
    public void setPoolSettings(PoolSettings poolSettings) {
        this.poolSettings = poolSettings;
    }

    @Override
    public void setTaskConfig(BootstrapMachineConfig taskConfig) {
        this.taskConfig = taskConfig;
    }
}
