package cloudify.widget.pool.manager.tasks;

import cloudify.widget.api.clouds.CloudExecResponse;
import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.api.clouds.ISshDetails;
import cloudify.widget.pool.manager.CloudServerApiFactory;
import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.*;
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
    private ErrorsDao errorsDao;


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

        String script = getBootstrapScript();

        script = injectBootstrapProperties(script);

        CloudServerApi cloudServerApi = CloudServerApiFactory.create(poolSettings.getProvider().getName());
        cloudServerApi.connect(poolSettings.getProvider().getConnectDetails());
        ISshDetails sshDetails = nodeModel.machineSshDetails;

        runBootstrapScriptOnMachine(script, cloudServerApi, sshDetails);

        return null;
    }

    private String getBootstrapScript() {
        return readScriptFromFile(getScriptFile());
    }

    private File getScriptFile() {
        File scriptFile;
        try {
            scriptFile = ResourceUtils.getFile(taskConfig.getBootstrapScriptResourcePath());
            logger.debug("bootstrap script file is [{}]", scriptFile);
        } catch (FileNotFoundException e) {
            String message = "failed to get resource for bootstrap script";
            logger.error(message, e);
            errorsDao.create(new ErrorModel()
                            .setPoolId(poolSettings.getUuid())
                            .setTaskName(getTaskName())
                            .setMessage(message)
            );
            throw new RuntimeException(message);
        }
        return scriptFile;
    }

    private String readScriptFromFile(File scriptFile) {
        String script;
        try {
            script = FileUtils.readFileToString(scriptFile);
            logger.debug("script file read to string\n\n[{}]...", script.substring(0, 20));
        } catch (IOException e) {
            String message = "failed to read bootstrap script file to string";
            logger.error(message, e);
            errorsDao.create(new ErrorModel()
                            .setPoolId(poolSettings.getUuid())
                            .setTaskName(getTaskName())
                            .setMessage(message)
            );
            throw new RuntimeException(message);
        }
        return script;
    }

    private String injectBootstrapProperties(String script) {
        BootstrapProperties bootstrapProperties = poolSettings.getBootstrapProperties();
        return script
                .replaceAll("##publicip##", bootstrapProperties.getPublicIp())
                .replaceAll("##privateip##", bootstrapProperties.getPrivateIp())
                .replaceAll("##cloudifyUrl##", bootstrapProperties.getCloudifyUrl())
                .replaceAll("##prebootstrapScript##", bootstrapProperties.getPreBootstrapScript())
                .replaceAll("##recipeRelativePath##", bootstrapProperties.getRecipeRelativePath())
                .replaceAll("##recipeUrl##", bootstrapProperties.getRecipeUrl());
    }

    private void runBootstrapScriptOnMachine(String script, CloudServerApi cloudServerApi, ISshDetails sshDetails) {
        updateNodeModelStatus(NodeStatus.BOOTSTRAPPING);
        CloudExecResponse cloudExecResponse = cloudServerApi.runScriptOnMachine(script, sshDetails);
        int exitStatus = cloudExecResponse.getExitStatus();
        String output = cloudExecResponse.getOutput();
        logger.info("finished running bootstrap on node [{}], exit status is [{}]", taskConfig.getNodeModel().id, exitStatus);
        logger.info("- - - bootstrap script execution output - - - \n{}", output);
        if (exitStatus == 0 && output.contains(taskConfig.getBootstrapSuccessText())) {
            updateNodeModelStatus(NodeStatus.BOOTSTRAPPED);
        } else {
            updateNodeModelStatus(NodeStatus.EXPIRED);
            String message = "bootstrap script execution failed";
            logger.error(message);
            HashMap<String, Object> infoMap = new HashMap<String, Object>();
            infoMap.put("exitStatus", exitStatus);
            infoMap.put("output", output);
            errorsDao.create(new ErrorModel()
                            .setPoolId(poolSettings.getUuid())
                            .setTaskName(getTaskName())
                            .setMessage(message)
                            .setInfoFromMap(infoMap)
            );
            throw new RuntimeException(message);
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
