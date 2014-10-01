package cloudify.widget.pool.manager.node_management;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * User: eliranm
 * Date: 5/5/14
 * Time: 5:58 PM
 */
public class NodeManagementModuleProvider implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    public BaseNodeManagementModule fromType(NodeManagementModuleType type) {
        switch (type) {
            case CREATE:
                return applicationContext.getBean(CreateNodeManagementModule.class);
            case DELETE:
                return applicationContext.getBean(DeleteNodeManagementModule.class);
            case BOOTSTRAP:
                return applicationContext.getBean(BootstrapNodeManagementModule.class);
            case DELETE_EXPIRED:
                return applicationContext.getBean(DeleteExpiredNodeManagementModule.class);
            case MARK_EXPIRED_PING:
                return applicationContext.getBean(PingNodeManagementModule.class);
        }
        throw new RuntimeException("unable to return module - no valid type provided");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
