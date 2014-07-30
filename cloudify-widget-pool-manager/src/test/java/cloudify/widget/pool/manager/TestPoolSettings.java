package cloudify.widget.pool.manager;

import cloudify.widget.common.GsObjectMapper;
import cloudify.widget.pool.manager.dto.PoolSettings;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sefi on 7/29/14.
 */
public class TestPoolSettings {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    /**
     * Tests if the JSON is a valid {@link cloudify.widget.pool.manager.dto.PoolSettings} when email configuration
     * is missing and description is present
     *
     * @throws IOException
     */
    @Test
    public void testPoolSettingsJson() throws IOException {
        InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("pool-settings.json");
        String s = IOUtils.toString(resourceAsStream);

        // expects an exception to be thrown
        exception.expect(UnrecognizedPropertyException.class);
        GsObjectMapper objectMapper = new GsObjectMapper();
        PoolSettings poolSettings = objectMapper.readValue(s, PoolSettings.class);
        System.out.println("poolSettings = " + poolSettings);

        // expects no exception
        exception = ExpectedException.none();
        objectMapper.removeFailOnUnknownProperties();
        poolSettings = objectMapper.readValue(s, PoolSettings.class);
        System.out.println("poolSettings = " + poolSettings);

        // expects an exception to be thrown
        exception.expect(UnrecognizedPropertyException.class);
        objectMapper.addFailOnUnknownProperties();
        poolSettings = objectMapper.readValue(s, PoolSettings.class);
        System.out.println("poolSettings = " + poolSettings);

        // reset exception rule.
        exception = ExpectedException.none();
    }
}
