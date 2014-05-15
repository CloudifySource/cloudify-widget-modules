package cloudify.widget.pool.manager.dto;

import java.util.HashMap;
import java.util.Map;

/**
 * User: eliranm
 * Date: 3/2/14
 * Time: 1:33 PM
 */
public class PoolStatus {

    private String poolId;
    private Map<NodeStatus,Integer> countPerNodeStatus = new HashMap<NodeStatus, Integer>();

    public int getCurrentSize() {
        int size = 0;
        Map<NodeStatus, Integer> countPerStatus = getCountPerNodeStatus();
        for (Integer integer : countPerStatus.values()) {
            size += integer;
        }
        return size;
    }

    public Map<NodeStatus, Integer> getCountPerNodeStatus() {
        return countPerNodeStatus;
    }

    public void setCountPerNodeStatus(Map<NodeStatus, Integer> countPerNodeStatus) {
        this.countPerNodeStatus = countPerNodeStatus;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PoolStatus that = (PoolStatus) o;

        if (countPerNodeStatus != null ? !countPerNodeStatus.equals(that.countPerNodeStatus) : that.countPerNodeStatus != null)
            return false;
        if (poolId != null ? !poolId.equals(that.poolId) : that.poolId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = poolId != null ? poolId.hashCode() : 0;
        result = 31 * result + (countPerNodeStatus != null ? countPerNodeStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PoolStatus{" +
                "poolId='" + poolId + '\'' +
                ", countPerStatus=" + countPerNodeStatus +
                '}';
    }
}
