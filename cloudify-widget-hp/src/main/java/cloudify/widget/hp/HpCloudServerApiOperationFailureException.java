package cloudify.widget.hp;

/**
 * User: evgeny
 * Date: 2/12/14
 * Time: 10:26 AM
 */
public class HpCloudServerApiOperationFailureException extends RuntimeException {

    public HpCloudServerApiOperationFailureException() {
        super();
    }

    public HpCloudServerApiOperationFailureException(String message) {
        super(message);
    }

    public HpCloudServerApiOperationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public HpCloudServerApiOperationFailureException(Throwable cause) {
        super(cause);
    }
}