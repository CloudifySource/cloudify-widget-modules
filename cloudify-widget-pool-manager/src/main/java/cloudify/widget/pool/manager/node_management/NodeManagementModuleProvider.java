package cloudify.widget.pool.manager.node_management;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: eliranm
 * Date: 5/5/14
 * Time: 5:58 PM
 */
public class NodeManagementModuleProvider {

    @Autowired
    private CreateNodeManagementModule createNodeManagementModule;

    @Autowired
    private DeleteNodeManagementModule deleteNodeManagementModule;

    @Autowired
    private BootstrapNodeManagementModule bootstrapNodeManagementModule;

    @Autowired
    private DeleteExpiredNodeManagementModule deleteExpiredNodeManagementModule;

    public BaseNodeManagementModule fromType(NodeManagementModuleType type) {
        switch (type) {
            case CREATE:
                return createNodeManagementModule;
            case DELETE:
                return deleteNodeManagementModule;
            case BOOTSTRAP:
                return bootstrapNodeManagementModule;
            case DELETE_EXPIRED:
                return deleteExpiredNodeManagementModule;
        }
        throw new RuntimeException("unable to return module - no valid type provided");
    }
}
