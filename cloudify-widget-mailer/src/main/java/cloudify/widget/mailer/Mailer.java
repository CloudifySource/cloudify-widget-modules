package cloudify.widget.mailer;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 2/11/14
 * Time: 6:26 PM
 */
public class Mailer {

    private static Logger logger = LoggerFactory.getLogger(Mailer.class);

    private final MailerConfig mailerConfig;

    public Mailer(MailerConfig mailerConfig) {
        logger.trace("initializing mailer with configuration [{}]", mailerConfig);
        this.mailerConfig = mailerConfig;
    }

    public void send(Mail mail) {

        assert mailerConfig != null : "mailer config cannot be null";

        Email email = new SimpleEmail();
        email.setDebug(false);
        email.setSmtpPort(mailerConfig.getSmtpPort());
        email.setAuthenticator(mailerConfig.getAuthenticator());
        email.setHostName(mailerConfig.getHostName());
        email.setStartTLSEnabled(mailerConfig.isTlsEnabled());

        try {
            email.setFrom(mail.from());
            email.setSubject(mail.subject());
            email.setMsg(mail.message());
            for (String to : mail.to()) {
                email.addTo(to);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("attempting send mail [{}]", mail);
            }
            email.send();

        } catch (Exception e) {
            logger.error("failed to send mail [{}] with configuration [{}]", mail, mailerConfig);
            throw new MailerException(String.format("failed to send mail from [%s] to [%s]", mail.from(), mail.to()), e);
        }
    }
}
