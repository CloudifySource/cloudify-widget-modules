package mocks.cloudify.widget;

import cloudify.widget.pool.manager.NodesDao;
import cloudify.widget.pool.manager.dto.NodeStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 9/30/14
 * Time: 9:34 PM
 */
public class NodesDaoMock extends NodesDao{

    @Override
    public List<Long> readExpiredIdsOfPool(String poolId) {
        List<Long> result = new LinkedList<Long>();
        result.add(2L);
        return result;
    }

    @Override
    public int updateStatus(long nodeId, NodeStatus nodeStatus) {
        return 0;
    }

    @Override
    public List<Long> readIdsOfPoolWithStatus(String poolId, NodeStatus nodeStatus) {
        List<Long> result = new LinkedList<Long>();
        result.add(2L);
        return result;
    }
}
