package cloudify.widget.pool.manager;

import cloudify.widget.common.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 2/27/14
 * Time: 3:33 PM
 */
public class SettingsFileLoader {

    private static Logger logger = LoggerFactory.getLogger(SettingsFileLoader.class);

    private String settingsFilePath;

    private String content;


    public String asString() {
        if (content == null) {
            content = fromFile();
        }
        return content;
    }

    private String fromFile() {
        if (settingsFilePath == null) {
            throw new RuntimeException("manager settings file not configured, please fix configuration for managerSettingsFilePath");
        }

        String content = FileUtils.readFileInClasspathToString(settingsFilePath);
        logger.debug("manager settings file read, content is [{}]", content);
        return content;
    }

    public void setSettingsFilePath(String settingsFilePath) {
        this.settingsFilePath = settingsFilePath;
    }

}
