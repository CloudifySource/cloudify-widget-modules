package cloudify.widget.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

/**
 * User: eliranm
 * Date: 3/4/14
 * Time: 1:24 PM
 */
public class FileUtils {

    private static Logger logger = LoggerFactory.getLogger(FileUtils.class);

    private FileUtils() {}


    public static String readFileInClasspathToString(String filePath) {
        File file = getFileInClasspath(filePath);
        String content = null;
        try {
            content = org.apache.commons.io.FileUtils.readFileToString(file);
        } catch (IOException e) {
            logger.error(String.format("failed to read file to string from path [%s]", filePath), e);
        }
        return content;
    }

    public static File getFileInClasspath(String filePath) {
        ClassPathResource resource = new ClassPathResource(filePath);
        File file = null;
        try {
            file = resource.getFile();
        } catch (IOException e) {
            logger.error(String.format("failed to read file from path [%s]", filePath), e);
        }
        return file;
    }

}
