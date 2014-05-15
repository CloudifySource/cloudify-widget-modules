package cloudify.widget.pool.manager.node_management;

import cloudify.widget.common.CollectionUtils;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.NodeStatus;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* User: eliranm
* Date: 4/28/14
* Time: 3:27 PM
*/
public class DeleteNodeManager extends NodeManager<DeleteNodeManager> {

    private static Logger logger = LoggerFactory.getLogger(DeleteNodeManager.class);


    @Override
    public DeleteNodeManager decide() {

        Constraints constraints = getConstraints();
        int maxNodes = constraints.maxNodes;
        int minNodes = constraints.minNodes;
        String uuid = constraints.poolSettings.getUuid();
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(uuid);

        Set<String> expiredIds = new HashSet<String>(nodesDao.readIdsOfPoolWithNodeStatus(uuid, NodeStatus.EXPIRED));
        Set<String> unoccupiedIds = new HashSet<String>();
        // try not to remove occupied nodes
        CollectionUtils.collect(nodeModels, new Transformer() {
            @Override
            public Object transform(Object input) {
                NodeModel n = (NodeModel) input;
                if (n.nodeStatus == NodeStatus.OCCUPIED) {
                    return null;
                }
                return n.machineId;
            }
        }, unoccupiedIds);

        // TBD
//        if (maxNodes - minNodes - toDelete.size() < 0) {
//        }

        int nodeModelsSize = nodeModels.size();

        logger.info("minNodes [{}], maxNodes [{}], nodeModelsSize [{}]", minNodes, maxNodes, nodeModelsSize);

        DeleteDecisionDetails decisionDetails = new DeleteDecisionDetails()
                .addMachineIds(expiredIds)
                .addMachineIds(unoccupiedIds);

        // queue it! (persist)

//        logger.info("queuing decision [{}]", deleteDecision);

        return this;

        // TODO write errors
    }

    @Override
    public DeleteNodeManager execute() {

        // TODO implement

        logger.info("executing decision [{}]", this);

        switch (mode) {
            case AUTO_APPROVAL:

                break;
            case MANUAL_APPROVAL:

                break;
            case MANUAL:

                break;
        }

        return this;
    }

}
