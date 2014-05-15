package cloudify.widget.mailer;

/**
 * User: eliranm
 * Date: 2/12/14
 * Time: 2:03 PM
 */
public class MailerException extends RuntimeException {

    public MailerException() {
        super();
    }

    public MailerException(String message) {
        super(message);
    }

    public MailerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MailerException(Throwable cause) {
        super(cause);
    }

/*    protected MailerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }*/
}
