package cloudify.widget.website.handlers;

import cloudify.widget.pool.manager.NodeManagementExecutor;
import cloudify.widget.website.dao.IPoolDao;
import cloudify.widget.website.models.PoolConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.List;

/**
 * User: eliranm
 * Date: 5/12/14
 * Time: 1:41 AM
 */
public class NodeManagementRecoveryHandler implements ApplicationListener<ContextRefreshedEvent> {

    private static Logger logger = LoggerFactory.getLogger(NodeManagementRecoveryHandler.class);

    @Autowired
    private IPoolDao poolDao;

    @Autowired
    private NodeManagementExecutor nodeManagementExecutor;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("> context refreshed - recovering node management for existing pool settings");
        List<PoolConfigurationModel> pools = poolDao.readPools();
        for (PoolConfigurationModel pool : pools) {
            nodeManagementExecutor.start(pool.getPoolSettings());
        }
    }
}
