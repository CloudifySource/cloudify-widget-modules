package cloudify.widget.pool.manager.dto;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/12/14
 * Time: 1:54 PM
 */
public class PoolStatusCount{
    private NodeStatus nodeStatus;
    private int count;
    private String poolId;

    public NodeStatus getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

        PoolStatusCount that = (PoolStatusCount) o;

        if (count != that.count) return false;
        if (poolId != null ? !poolId.equals(that.poolId) : that.poolId != null) return false;
        if (nodeStatus != that.nodeStatus) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = nodeStatus != null ? nodeStatus.hashCode() : 0;
        result = 31 * result + count;
        result = 31 * result + (poolId != null ? poolId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PoolStatusCount{" +
                "status=" + nodeStatus +
                ", count=" + count +
                ", poolId='" + poolId + '\'' +
                '}';
    }
}
