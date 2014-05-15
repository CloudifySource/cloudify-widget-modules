package cloudify.widget.website.interceptors;

import cloudify.widget.common.StringUtils;
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

public class AuthenticationInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);

    @Autowired
    private IAccountDao accountDao;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception{
        logger.info("in interceptor");
        String accountUuid = request.getHeader("AccountUuid");



        if ( StringUtils.isEmpty(accountUuid) ){
            response.sendError(401, "{'message' : 'account uuid missing on request header'}");
            return false;
        }

        AccountModel accountModel = accountDao.readAccountByUuid(accountUuid);

        if ( accountModel == null ){
            response.sendError(401, "{'message' : 'Account with uuid [" + accountUuid + "] not found'}");
            return false;
        }

        request.setAttribute("account", accountModel);

        return super.preHandle(request, response, handler);
    }

    public IAccountDao getAccountDao() {
        return accountDao;
    }

    public void setAccountDao(IAccountDao accountDao) {
        this.accountDao = accountDao;
    }
}