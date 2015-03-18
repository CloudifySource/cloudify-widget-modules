package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.PoolManagerApi;
import cloudify.widget.pool.manager.dto.DecisionModel;
import cloudify.widget.pool.manager.dto.NodeMappings;
import cloudify.widget.pool.manager.tasks.TaskCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by guym on 3/17/15.
 */
public class GhostsManagementModule extends BaseNodeManagementModule<GhostsManagementModule, GhostDecisionDetails>{

    private static Logger logger = LoggerFactory.getLogger(GhostsManagementModule.class);

    private Map<String, GhostDecisionDetails> data = new HashMap<String, GhostDecisionDetails>();

    @Autowired
    private PoolManagerApi poolManagerApi;



    @Override
    public GhostsManagementModule decide() {
        List<NodeMappings> nodeMappings = poolManagerApi.listCloudNodes(getConstraints().poolSettings);
        Map<String, GhostDecisionDetails> newData = new HashMap<String, GhostDecisionDetails>();
        for (NodeMappings nodeMapping : nodeMappings) {
            if ( nodeMapping.getNodeId() != -1 ){ // not a real ghost.
                continue;
            }
            String machineId = nodeMapping.getMachineId();
            if ( data.containsKey(machineId) ){
                newData.put(machineId, data.get(machineId)); // keep existing records
            }else{
                newData.put(machineId, new GhostDecisionDetails(nodeMapping, getConstraints().poolSettings.getGhostTimeLimit())); // add new records
            }
        }

        data = newData; // remove old records.

        for (GhostDecisionDetails ghostDecisionDetails : data.values()) {

            if (!ghostDecisionDetails.isCalledGhostbusters() && ghostDecisionDetails.isHaunting()) {
                ghostDecisionDetails.setCalledGhostbusters(true);
                DecisionModel decisionModel = buildOwnDecisionModel(ghostDecisionDetails);
                decisionsDao.create(decisionModel);
            }
        }

        return this;
    }

    @Override
    protected void executeDecision(final DecisionModel dm) {
        final Map<String, GhostDecisionDetails> fData = data;
        final GhostDecisionDetails details = (GhostDecisionDetails) dm.details;

        logger.info("creating machine instance via pool manager task executor");
        poolManagerApi.deleteCloudNode(getConstraints().poolSettings,  details.getMachineId(), new TaskCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                teardownDecisionExecution(dm);
                fData.remove(details.getMachineId());
            }

            @Override
            public void onFailure(Throwable t) {
                teardownDecisionExecution(dm);
                fData.remove(details.getMachineId());
            }
        });
    }

    @Override
    public NodeManagementModuleType getType() {
        return NodeManagementModuleType.GHOST;
    }

    public PoolManagerApi getPoolManagerApi() {
        return poolManagerApi;
    }

    public void setPoolManagerApi(PoolManagerApi poolManagerApi) {
        this.poolManagerApi = poolManagerApi;
    }
}
