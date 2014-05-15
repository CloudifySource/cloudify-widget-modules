package cloudify.widget.website.exceptions;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 6:23 PM
 */
public class Forbidden extends BaseException {
    public Forbidden( String message) {
        super(401, message);
    }

    public Forbidden( String message, Map<String, Object> info) {
        super(401, message, info);
    }
}
