package cloudify.widget.pool.manager;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.CloudServerApi;
import cloudify.widget.pool.manager.dto.NodeMappings;
import cloudify.widget.pool.manager.dto.NodeModel;
import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.dto.ProviderSettings;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * User: eliranm
 * Date: 3/24/14
 * Time: 1:55 PM
 */
public class NodeMappingsDao {

    @Autowired
    private NodesDao nodesDao;

    public List<NodeMappings> readAll(PoolSettings poolSettings) {

        // connect to the cloud and get servers
        ProviderSettings provider = poolSettings.getProvider();
        CloudServerApi cloudServerApi = CloudServerApiFactory.create(provider.getName());
        cloudServerApi.connect(poolSettings.getProvider().getConnectDetails());
        Collection<CloudServer> cloudServers = cloudServerApi.listByMask(provider.getMachineOptions().getMask());

        // get current node models representation
        List<NodeModel> nodeModels = nodesDao.readAllOfPool(poolSettings.getUuid());

        // merge and create objects accordingly
        Collection<NodeMappings> result = _transform(cloudServers, nodeModels);

        return new ArrayList<NodeMappings>(result);
    }

    private Collection<NodeMappings> _transform(Collection<CloudServer> cloudServers, Collection<NodeModel> nodeModels) {

        // assign all items to map based on common grounds (machine id)
        final Map<String /* machine id */, NodeMappings> resultMap = new HashMap<String, NodeMappings>();
        final Map<String /* machine id */, NodeModel> nodeModelsMap = new HashMap<String, NodeModel>(nodeModels.size());
        final Map<String /* machine id */, CloudServer> cloudServersMap = new HashMap<String, CloudServer>(cloudServers.size());
        for (NodeModel nodeModel : nodeModels) {
            nodeModelsMap.put(nodeModel.machineId, nodeModel);
        }
        for (CloudServer cloudServer : cloudServers) {
            cloudServersMap.put(cloudServer.getId(), cloudServer);
        }

        // map from the larger map to the smaller map, to go through all possible combinations
        if (cloudServersMap.size() >= nodeModelsMap.size()) {
            _mapCloudServersToNodeModels(resultMap, nodeModelsMap, cloudServersMap);
        } else {
            _mapNodeModelsToCloudServers(resultMap, nodeModelsMap, cloudServersMap);
        }

        return resultMap.values();
    }

    private static void _mapNodeModelsToCloudServers(Map<String, NodeMappings> resultMap, Map<String, NodeModel> nodeModelsMap, Map<String, CloudServer> cloudServersMap) {
        for (NodeModel nodeModel : nodeModelsMap.values()) {
            NodeMappings nodeMappings = new NodeMappings();

            CloudServer cloudServer = cloudServersMap.get(nodeModel.machineId);
            if (cloudServer != null) {
                nodeMappings.setIp(cloudServer.getServerIp());
            }
            nodeMappings.setMachineId(nodeModel.machineId);
            nodeMappings.setNodeId(nodeModel.id);

            resultMap.put(nodeModel.machineId, nodeMappings);
        }
    }

    private static void _mapCloudServersToNodeModels(Map<String, NodeMappings> resultMap, Map<String, NodeModel> nodeModelsMap, Map<String, CloudServer> cloudServersMap) {
        for (CloudServer cloudServer : cloudServersMap.values()) {
            NodeMappings nodeMappings = new NodeMappings();

            NodeModel nodeModel = nodeModelsMap.get(cloudServer.getId());
            if (nodeModel != null) {
                nodeMappings.setNodeId(nodeModel.id);
            }
            nodeMappings.setIp(cloudServer.getServerIp());
            nodeMappings.setMachineId(cloudServer.getId());

            resultMap.put(cloudServer.getId(), nodeMappings);
        }
    }

}
