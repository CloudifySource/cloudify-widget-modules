package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.NodeManagementModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: eliranm
 * Date: 4/28/14
 * Time: 5:46 PM
 */
public class DeleteExpiredNodeManagementModule extends BaseNodeManagementModule<DeleteExpiredNodeManagementModule> {

    private static Logger logger = LoggerFactory.getLogger(DeleteExpiredNodeManagementModule.class);

    @Override
    public DeleteExpiredNodeManagementModule decide() {
        return this;
    }

    @Override
    public DeleteExpiredNodeManagementModule execute() {
        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.DELETE_EXPIRED;
    }
}
