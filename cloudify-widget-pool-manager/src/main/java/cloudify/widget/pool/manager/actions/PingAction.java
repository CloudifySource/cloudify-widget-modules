package cloudify.widget.pool.manager.actions;

import cloudify.widget.pool.manager.dto.PingSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Created by sefi on 9/9/14.
 */
public class PingAction {

    private static final Logger logger = LoggerFactory.getLogger(PingAction.class);

    /**
     * <p>Given a hostname/IP and PingSettings it tries to ping and return the result.<br>
     * It supports a number of retries with timeout as defined in the pingSettings and compares the
     * responseCode against the defined whiteList.</p>
     *
     * @param host
     *          The hostname / IP
     * @param pingSettings
     *          The pingSettings
     * @return
     *          True if ping was successful, false otherwise
     */
    public Boolean ping(String host, PingSettings pingSettings) {
        String url = pingSettings.getUrl().replace("$HOST", host);
        Boolean pingResult = false;
        logger.debug("starting ping for " + url);

        for (int i = 0; i < pingSettings.getRetryCount(); i++) {
            // get ping result
            int responseCode = getResponseCode(url, pingSettings.getPingTimeout());
            pingResult = isWhiteListed(responseCode, pingSettings.getWhiteList());

            if (pingResult) {
                logger.debug("Ping successful!");
                break;
            }
        }

        if (!pingResult) {
            logger.debug("Ping failed!");
        }

        return pingResult;
    }

    /**
     * <p>Check the response code against the white list. Only if the code is in the white list it is considered
     * a successful ping.</p>
     *
     * <p>So, getting a 200 response code that is not in the white list will result in a failed ping, while
     * getting a 500 response code that is in the list will result in a successful ping.</p>
     *
     * <p>A response code of -1 means that the request failed to gp through and no response code was received.<br>
     * See {@link #getResponseCode(String, int)} for more info.</p>
     *
     * @param responseCode
     *          The response code to match
     * @param whiteList
     *          The list of codes to match against
     * @return
     *          True if matched, false otherwise.
     */
    private Boolean isWhiteListed(int responseCode, List<String> whiteList) {
        Boolean found = false;

        if (responseCode != -1) {
            logger.debug("Checking if responseCode " + responseCode + " is in white list");
            // no errors getting the code, check if it is white listed.
            for (String item : whiteList) {
                int parsed = Integer.parseInt(item);

                if (responseCode == parsed) {
                    found = true;
                    logger.debug("responseCode " + responseCode + " is in white list");
                    break;
                }
            }
        }

        return found;
    }

    /**
     * This does the actual ping. Supports HTTP as well as HTTPS.
     *
     * @param url
     *          The URL to ping
     * @param timeout
     *          The ping request timeout
     * @return
     *          the response code, or -1 if an error occurred.
     */
    private int getResponseCode(String url, int timeout) {
        try {
            if (url.startsWith("https")) {
                logger.debug("URL is HTTPS, ignoring invalid certificates...");
                // Ignore invalid certificate
                initHttpsConnection();
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                return connection.getResponseCode();
            } else {
                logger.debug("URL is HTTP");
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                return connection.getResponseCode();
            }

        } catch (Exception e) {
            logger.error("Ping failed for [" + url + "] with timeout [" + timeout + "]");
            return -1;

        }
    }

    private void initHttpsConnection() throws NoSuchAlgorithmException, KeyManagementException {
        logger.debug("Setting up the HTTPS connection to ignore invalid certificates");
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] { new MyX509TrustManager() };
        // Install the all-trusting trust manager
        final SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private static class MyX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

}
