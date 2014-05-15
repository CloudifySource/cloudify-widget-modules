package cloudify.widget.website.exceptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 6:21 PM
 */
public class BaseException extends RuntimeException{
    public int status;
    public String message;
    public Map<String,Object> info = new HashMap<String, Object>();


    public BaseException(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public BaseException(int status, String message, Map<String, Object> info) {
        this.status = status;
        this.message = message;
        this.info = info;
    }
}
