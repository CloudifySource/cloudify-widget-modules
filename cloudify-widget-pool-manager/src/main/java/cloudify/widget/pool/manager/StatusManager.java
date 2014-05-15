package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.PoolSettings;
import cloudify.widget.pool.manager.dto.PoolStatus;
import cloudify.widget.pool.manager.dto.PoolStatusCount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 8:05 PM
 */
public class StatusManager {

    private static Logger logger = LoggerFactory.getLogger(StatusManager.class);

    @Autowired
    private NodesDao nodesDao;

    public void updateStatus(PoolStatus poolStatus) {
    }

    public void setStatus(PoolStatus poolStatus) {
    }

    public Collection<PoolStatus> listPoolStatuses() {
        List<PoolStatusCount> poolStatusCounts = nodesDao.getPoolStatusCounts();
        return _getPoolStatuses(poolStatusCounts);
    }

    public PoolStatus getPoolStatus( PoolSettings poolSettings ) {
        List<PoolStatusCount> poolStatusCountsOfPool = nodesDao.getPoolStatusCountsOfPool(poolSettings.getUuid());
        Collection<PoolStatus> poolStatuses = _getPoolStatuses(poolStatusCountsOfPool);
        if (!poolStatuses.isEmpty() && poolStatuses.size() == 1) {
            return poolStatuses.iterator().next();
        }
        return null;
    }

    private Collection<PoolStatus> _getPoolStatuses(List<PoolStatusCount> poolStatusCounts) {
        Map<String /* poolId */, PoolStatus> poolIdToPoolStatusMap = new HashMap<String, PoolStatus>();
        for (PoolStatusCount poolStatusCount : poolStatusCounts) {
            String poolId = poolStatusCount.getPoolId();
            // init if not exist
            if ( !poolIdToPoolStatusMap.containsKey(poolId) ){
                PoolStatus poolStatus = new PoolStatus();
                poolStatus.setPoolId(poolId);
                poolIdToPoolStatusMap.put(poolId, poolStatus);
            }
            // setting count per status
            poolIdToPoolStatusMap.get(poolId).getCountPerNodeStatus().put( poolStatusCount.getNodeStatus(), poolStatusCount.getCount() );
        }
        return poolIdToPoolStatusMap.values();
    }

}
