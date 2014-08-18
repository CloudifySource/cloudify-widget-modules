package cloudify.widget.pool.manager;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by sefi on 8/17/14.
 */
public class BootstrapScriptLoader {

    private static final Logger logger = LoggerFactory.getLogger(BootstrapScriptLoader.class);
    private String bootstrapScriptResourcePath;

    public void setBootstrapScriptResourcePath(String bootstrapScriptResourcePath) {
        this.bootstrapScriptResourcePath = bootstrapScriptResourcePath;
    }

    private File getScriptFile() {
        File scriptFile;
        String bootstrapScriptResourcePath = this.bootstrapScriptResourcePath;

        try {
            scriptFile = ResourceUtils.getFile(bootstrapScriptResourcePath);
            logger.debug("bootstrap script file is [{}]", scriptFile);
        } catch (FileNotFoundException e) {
            String message = "failed to get resource for bootstrap script from [" + bootstrapScriptResourcePath + "]";
            logger.error(message, e);
            throw new RuntimeException(message);
        }
        return scriptFile;
    }

    public String readScriptFromFile() {
        File scriptFile = getScriptFile();
        String script;
        try {
            script = FileUtils.readFileToString(scriptFile);
            logger.debug("script file read to string\n\n[{}]...", script.substring(0, 20));
        } catch (IOException e) {
            String message = "failed to read bootstrap script file to string from [" + scriptFile.getAbsolutePath() + "]";
            logger.error(message, e);
            throw new RuntimeException(message);
        }
        return script;
    }
}
