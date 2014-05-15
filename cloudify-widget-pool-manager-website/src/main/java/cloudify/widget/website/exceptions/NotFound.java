package cloudify.widget.website.exceptions;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 6:21 PM
 */
public class NotFound extends BaseException {

    public NotFound(String message) {
        super(404, message);
    }

    public NotFound(String message, Map<String, Object> info) {
        super(404, message, info);
    }
}
