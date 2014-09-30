package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApiImpl;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import cloudify.widget.pool.manager.dto.PingResult;
import cloudify.widget.pool.manager.dto.PingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by sefi on 9/11/14.
 */
public class PingNodeManagementModule extends BaseNodeManagementModule<PingNodeManagementModule, PingDecisionDetails> {

    private static final Logger logger = LoggerFactory.getLogger(PingNodeManagementModule.class);

    @Autowired
    private PoolManagerApiImpl poolManagerApi;


    @Override
    public PingNodeManagementModule decide() {
        Constraints constraints = getConstraints();
        List<NodeModel> bootstrappedNodeModels = nodesDao.readAllOfPoolWithStatus(constraints.poolSettings.getUuid(), NodeStatus.BOOTSTRAPPED);

        // we have nothing to do if no bootstrapped nodes found
        if (bootstrappedNodeModels.isEmpty()) {
            logger.debug("no bootstrapped nodes to ping");
            return this;
        }

        for (NodeModel node : bootstrappedNodeModels) {
            logger.debug("Pinging node [{}]", node.id);
            PingResult pingResult = poolManagerApi.pingNode(constraints.poolSettings, node.id);

            if (pingResult.getPingStatus() == PingStatus.PING_FAIL) {
                logger.debug("Ping for node [{}] failed, marking it as EXPIRED", node.id);
                nodesDao.updateStatus(node.id, NodeStatus.EXPIRED);
            }
        }

        return this;
    }

    @Override
    public PingNodeManagementModule execute() {
        return this;
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.PING;
    }
}
