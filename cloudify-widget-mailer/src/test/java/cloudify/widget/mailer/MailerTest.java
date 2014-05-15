package cloudify.widget.mailer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertTrue;

/**
 * User: eliranm
 * Date: 2/11/14
 * Time: 6:27 PM
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mailer-test-context.xml"})
public class MailerTest {

    private static Logger logger = LoggerFactory.getLogger(MailerTest.class);

    @Autowired
    private MailerConfig mailerConfig;

    @Autowired
    private Mail.MailBuilder mailBuilder;


    @Test
    public void testSendMail() {

        logger.info("building mail, adding message body");
        Mail mail = mailBuilder.message("test message!").build();
        logger.info("created mail [{}]", mail);

        logger.info("creating new mailer from config [{}]", mailerConfig);
        Mailer mailer = new Mailer(mailerConfig);

        logger.info("sending mail with correct configuration...");
        Exception exception = null;
        try {
            mailer.send(mail);
        } catch (RuntimeException e) {
            exception = e;
            e.printStackTrace();
        } finally {
            assertTrue("exception should have not been thrown", exception == null);
        }

        logger.info("sending mail with wrong configuration...");
        mailerConfig.setHostName("blah-blah");
        exception = null;
        mailer = new Mailer(mailerConfig);
        try {
            mailer.send(mail);
        } catch (RuntimeException e) {
            exception = e;
        } finally {
            assertTrue("exception should have been thrown", exception != null);
            assertTrue("exception thrown should be a MailerException", MailerException.class.isAssignableFrom(exception.getClass()));
        }
    }

}
