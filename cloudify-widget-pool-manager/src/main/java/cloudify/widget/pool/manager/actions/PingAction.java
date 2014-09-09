package cloudify.widget.pool.manager.actions;

import cloudify.widget.pool.manager.dto.PingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by sefi on 9/9/14.
 */
public class PingAction {

    private static final Logger logger = LoggerFactory.getLogger(PingAction.class);

    //todo: IgnoreInvalideCertificate...

    public Boolean ping(String host, PingSettings pingSettings) {

        String url = pingSettings.getUrl().replace("$HOST", host);
        Boolean pingResult = false;

        // ping according to retry count
        for (int i = 0; i < pingSettings.getRetryCount(); i++) {
            // get ping result
            int responseCode = getResponseCode(url, pingSettings.getPingTimeout());
            pingResult = isWhiteListed(responseCode, pingSettings.getWhiteList());

            if (pingResult) {
                // success! no need to keep trying...
                break;
            }
        }

        return pingResult;
    }

    private Boolean isWhiteListed(int responseCode, List<String> whiteList) {
        Boolean found = false;

        if (responseCode != -1) {
            // no errors getting the code, check if it is white listed.
            for (String item : whiteList) {
                int parsed = Integer.parseInt(item);

                if (responseCode == parsed) {
                    found = true;
                    break;
                }
            }
        }

        return found;
    }

    private int getResponseCode(String url, int timeout) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeout);
            return connection.getResponseCode();

        } catch (Exception e) {
            logger.error("Ping failed for [" + url + "] with timeout [" + timeout + "]");
            return -1;

        }
    }
}
