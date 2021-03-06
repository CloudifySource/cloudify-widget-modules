package cloudify.widget.website.interceptors;

import cloudify.widget.common.StringUtils;
import cloudify.widget.website.config.AppConfig;
import cloudify.widget.website.dao.IAccountDao;
import cloudify.widget.website.models.AccountModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/5/14
 * Time: 5:03 PM
 */

public class AdminAuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AdminAuthenticationInterceptor.class);

    @Autowired
    private IAccountDao accountDao;

    @Autowired
    private AppConfig conf;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        logger.trace("in Admin interceptor");
        String accountUuid = request.getHeader("AccountUuid");

        if( StringUtils.isEmpty( accountUuid ) ){
            response.sendError(401, "{'message' : 'account uuid missing on request header'}");
            return false;
        }
        else{
             boolean accountUidsIdentical = StringUtils.equals( conf.getAdminUuid(), accountUuid );
            if( !accountUidsIdentical ){
                response.sendError(401, "{'message' : 'account uuid authentication failed'}");
                return false;
            }
        }

        logger.trace("intercepting...");
        return super.preHandle(request, response, handler);
    }

    public IAccountDao getAccountDao() {
        return accountDao;
    }

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }

    public AppConfig getConf() {
        return conf;
    }

    public void setConf(AppConfig conf) {
        this.conf = conf;
    }
}