package cloudify.widget.website.exceptions;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 6:23 PM
 */
public class InternalServerError extends BaseException {
    public InternalServerError( String message) {
        super(500, message);
    }

    public InternalServerError( String message, Map<String, Object> info) {
        super(500, message, info);
    }
}
