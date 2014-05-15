package cloudify.widget.website.interceptors;

import cloudify.widget.website.exceptions.BaseException;
import cloudify.widget.website.exceptions.InternalServerError;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 6:15 PM
 */
public class ExceptionInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(ExceptionInterceptor.class);
    private ObjectMapper mapper = new ObjectMapper();

    public void sendError(  HttpServletResponse response, BaseException baseException ) throws Exception{
        HashMap<String,Object> responseContent = new HashMap<String, Object>();
        responseContent.put("message", baseException.message);
        responseContent.put("info", baseException.info);

        String responseJson = mapper.writeValueAsString( responseContent );
        response.sendError( baseException.status, responseJson );
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.trace("got an exception : " + ex + " and response status is : " + response.getStatus());

        if( ex != null ){
            if (ex instanceof BaseException) {
                BaseException baseException = (BaseException) ex;
                sendError(response, baseException);
            } else {
                sendError(response, new InternalServerError("unknown error, caused by " + ex.getMessage()));
            }
        }

        super.afterCompletion(request, response, handler, null);
    }
}
