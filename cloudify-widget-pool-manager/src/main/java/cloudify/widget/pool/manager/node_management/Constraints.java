package cloudify.widget.pool.manager.node_management;

import cloudify.widget.pool.manager.dto.PoolSettings;

import java.util.ArrayList;

/**
* User: eliranm
* Date: 4/28/14
* Time: 3:22 PM
*/
public class Constraints {

    public PoolSettings poolSettings;

    public int minNodes;

    public int maxNodes;

    // TODO TBD default should be all active?
    public ArrayList<String> activeNodeManagers = new ArrayList<String>();

    public Constraints(PoolSettings ps) {
        if (ps == null) {
            throw new RuntimeException("pool settings must not be null");
        }

        poolSettings = ps;
        minNodes = ps.getMinNodes();
        maxNodes = ps.getMaxNodes();
//            activeNodeManagers = poolSettings.getActiveNodeManagers(); // TODO
    }


}
