package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.PoolSettings;

/**
* User: eliranm
* Date: 4/28/14
* Time: 3:22 PM
*/
public class Constraints {

    public PoolSettings poolSettings;

    public int minNodes;

    public int maxNodes;

    public NodeManagementMode nodeManagementMode;

    public Constraints(PoolSettings ps) {
        if (ps == null) {
            throw new RuntimeException("pool settings must not be null");
        }

        poolSettings = ps;
        minNodes = ps.getMinNodes();
        maxNodes = ps.getMaxNodes();
        nodeManagementMode = ps.getNodeManagement().getMode();
    }


}
