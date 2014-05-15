package cloudify.widget.mailer;

import javax.mail.Authenticator;

/**
 * User: eliranm
 * Date: 2/12/14
 * Time: 1:23 PM
 */
public class MailerConfig {

    private int smtpPort;
    private Authenticator authenticator;
    private String hostName;
    private boolean tlsEnabled;

    public MailerConfig(int smtpPort, Authenticator authenticator, String hostName, boolean tlsEnabled) {
        this.smtpPort = smtpPort;
        this.authenticator = authenticator;
        this.hostName = hostName;
        this.tlsEnabled = tlsEnabled;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public boolean isTlsEnabled() {
        return tlsEnabled;
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        this.tlsEnabled = tlsEnabled;
    }

    @Override
    public String toString() {
        return "MailerConfig{" +
                "smtpPort=" + smtpPort +
                ", authenticator=" + authenticator +
                ", hostName='" + hostName + '\'' +
                ", tlsEnabled=" + tlsEnabled +
                '}';
    }
}
