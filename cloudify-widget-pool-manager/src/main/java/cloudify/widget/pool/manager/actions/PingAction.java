package cloudify.widget.pool.manager.actions;

import cloudify.widget.pool.manager.dto.PingResponse;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sefi on 9/9/14.
 */
public class PingAction {

    private static final Logger logger = LoggerFactory.getLogger(PingAction.class);

    /**
     * Same as {@link #ping(String, cloudify.widget.pool.manager.dto.PingSettings)}, but for a list of PingSettings.
     *
     * @param host
     *          The hostname / IP
     * @param pingSettingsList
     *          The list of pingSettings
     * @return
     */
    public List<PingResponse> pingAll(String host, List<PingSettings> pingSettingsList) {
//        Boolean pingAllResult = true;
        List<PingResponse> pingAllResult = new ArrayList<PingResponse>();

        for (int i = 0; i < pingSettingsList.size(); i++) {
            PingSettings pingSettings = pingSettingsList.get(i);
            PingResponse pingResponse = ping(host, pingSettings);
            pingResponse.setPingSettings(pingSettings);
            pingAllResult.add(pingResponse);

//            if (!pingResponse.isWhiteListed()) {
//                pingAllResult = false;
//                break;
//            }
        }

        return pingAllResult;
    }

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
     *          PingResponse
     */
    public PingResponse ping(String host, PingSettings pingSettings) {
        String url = pingSettings.getUrl().replace("$HOST", host);
        logger.debug("starting ping for " + url);
        PingResponse pingResponse = new PingResponse();

        for (int i = 0; i < pingSettings.getRetryCount(); i++) {
            // get ping result
            pingResponse = getResponseCode(url, pingSettings.getPingTimeout());
            isWhiteListed(pingResponse, pingSettings.getWhiteList());

            if (pingResponse.isWhiteListed()) {
                logger.debug("Ping successful!");
                break;
            }
        }

        if (!pingResponse.isWhiteListed()) {
            logger.debug("Ping failed!");
        }

        return pingResponse;
    }

    /**
     * <p>Check the response code against the white list. Only if the code is in the white list it is considered
     * a successful ping.</p>
     *
     * <p>So, getting a 200 response code that is not in the white list will result in a failed ping, while
     * getting a 500 response code that is in the list will result in a successful ping.</p>
     *
     * <p>A response code of -1 means that the request failed to go through and no response code was received.<br>
     * See {@link #getResponseCode(String, int)} for more info.</p>
     *
     * @param pingResponse
     *          The PingResponse code to match
     * @param whiteList
     *          The list of codes to match against
     * @return
     *          True if matched, false otherwise.
     */
    private PingResponse isWhiteListed(PingResponse pingResponse, List<String> whiteList) {
        Boolean found = false;

        if (pingResponse.getResponseCode() != -1) {
            logger.debug("Checking if responseCode " + pingResponse.getResponseCode() + " is in white list");
            // no errors getting the code, check if it is white listed.
            for (String item : whiteList) {
                int parsed = Integer.parseInt(item);

                if (pingResponse.getResponseCode() == parsed) {
                    found = true;
                    logger.debug("responseCode " + pingResponse.getResponseCode() + " is in white list");
                    break;
                }
            }
        }

        pingResponse.setWhiteListed(found);
        return pingResponse;
    }

    /**
     * This does the actual ping. Supports HTTP as well as HTTPS.
     *
     * @param url
     *          The URL to ping
     * @param timeout
     *          The ping request timeout
     * @return
     *          the PingResponse
     */
    private PingResponse getResponseCode(String url, int timeout) {
        PingResponse pingResponse = new PingResponse();

        try {
            if (url.startsWith("https")) {
                logger.debug("URL is HTTPS, ignoring invalid certificates...");
                // Ignore invalid certificate
                initHttpsConnection();
                HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                pingResponse.setResponseCode(connection.getResponseCode());
            } else {
                logger.debug("URL is HTTP");
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setConnectTimeout(timeout);
                pingResponse.setResponseCode(connection.getResponseCode());
            }

        } catch (Exception e) {
            logger.error("Ping failed for [" + url + "] with timeout [" + timeout + "]");
            pingResponse.setResponseCode(-1);
            pingResponse.setErrorMessage(e.getMessage());
        }

        return pingResponse;
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
