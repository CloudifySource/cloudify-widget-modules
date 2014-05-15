package cloudify.widget.ec2;

/**
 * User: evgeny
 * Date: 2/12/14
 * Time: 10:26 AM
 */
public class Ec2CloudServerApiOperationFailureException extends RuntimeException {

    public Ec2CloudServerApiOperationFailureException() {
        super();
    }

    public Ec2CloudServerApiOperationFailureException(String message) {
        super(message);
    }

    public Ec2CloudServerApiOperationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public Ec2CloudServerApiOperationFailureException(Throwable cause) {
        super(cause);
    }
}