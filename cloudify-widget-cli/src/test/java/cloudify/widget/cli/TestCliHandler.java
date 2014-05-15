package cloudify.widget.cli;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 2/13/14
 * Time: 1:26 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:cli-context.xml"})
public class TestCliHandler implements ApplicationContextAware{
    private static Logger logger = LoggerFactory.getLogger(TestCliHandler.class);

    @Inject
    private ICloudBootstrapDetails bootstrapDetails;

    @Inject
    private ICloudifyCliHandler cliHandler;

    private ApplicationContext context;

    @Test
    public void testJsonBinding() throws IOException {
        Object object = context.getBean("detailsObject");
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = FileUtils.readFileToString(new ClassPathResource("softlayerBootstrapDetails.json").getFile());
        mapper.readerForUpdating(object).readValue(jsonString);
        logger.info(object.toString());

    }

    @Test
    public void testCliHandler(){
        File newCloudName = cliHandler.createNewCloud( bootstrapDetails );
        logger.info("created new cloud name [{}]", newCloudName);
    }


    public ICloudifyCliHandler getCliHandler() {
        return cliHandler;
    }

    public void setCliHandler(ICloudifyCliHandler cliHandler) {
        this.cliHandler = cliHandler;
    }

    public ICloudBootstrapDetails getBootstrapDetails() {
        return bootstrapDetails;
    }

    public void setBootstrapDetails(ICloudBootstrapDetails bootstrapDetails) {
        this.bootstrapDetails = bootstrapDetails;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
         context = applicationContext;
    }
}
