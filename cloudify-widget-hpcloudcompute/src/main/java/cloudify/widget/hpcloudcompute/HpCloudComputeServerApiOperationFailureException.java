package cloudify.widget.hpcloudcompute;

/**
 * User: evgeny
 * Date: 2/12/14
 * Time: 10:26 AM
 */
public class HpCloudComputeServerApiOperationFailureException extends RuntimeException {

    public HpCloudComputeServerApiOperationFailureException() {
        super();
    }

    public HpCloudComputeServerApiOperationFailureException(String message) {
        super(message);
    }

    public HpCloudComputeServerApiOperationFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public HpCloudComputeServerApiOperationFailureException(Throwable cause) {
        super(cause);
    }
}