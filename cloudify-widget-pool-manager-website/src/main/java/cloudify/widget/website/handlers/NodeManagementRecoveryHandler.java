package cloudify.widget.website.handlers;

import cloudify.widget.pool.manager.NodeManagementExecutor;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.node_management.DecisionsDao;
import cloudify.widget.website.dao.IPoolDao;
import cloudify.widget.website.models.PoolConfigurationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.LinkedList;
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
    private DecisionsDao decisionsDao;

    @Autowired
    private NodeManagementExecutor nodeManagementExecutor;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        List<PoolConfigurationModel> pools = poolDao.readPools();

        List<String> poolSettingsIds = new LinkedList<String>();
        List<PoolSettings> poolSettingsList = new LinkedList<PoolSettings>();
        for (PoolConfigurationModel pool : pools) {
            poolSettingsIds.add(pool.getPoolSettings().getUuid());
            poolSettingsList.add(pool.getPoolSettings());
        }

        logger.info("deleting any decisions not attached to existing pool settings");
        decisionsDao.deleteAllNotOfPools(poolSettingsIds);

        logger.info("recovering node management for existing pool settings [{}]", poolSettingsIds);
        for (PoolSettings poolSettings : poolSettingsList) {
            nodeManagementExecutor.start(poolSettings);
        }
    }
}
