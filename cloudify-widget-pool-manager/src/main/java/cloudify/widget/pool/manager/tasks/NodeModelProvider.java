package cloudify.widget.pool.manager.tasks;

import cloudify.widget.pool.manager.dto.NodeModel;

/**
 * User: eliranm
 * Date: 3/19/14
 * Time: 1:47 PM
 */
public interface NodeModelProvider {

    NodeModel getNodeModel();
}
