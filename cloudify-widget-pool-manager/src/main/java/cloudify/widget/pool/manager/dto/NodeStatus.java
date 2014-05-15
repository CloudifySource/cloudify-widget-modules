package cloudify.widget.pool.manager.dto;

/**
 * Statuses for nodes are sequential, so order is important.
 */
public enum NodeStatus {

    /* don't change the order of constants! */
    CREATING,
    CREATED,
    BOOTSTRAPPING,
    BOOTSTRAPPED,
    OCCUPIED,
    EXPIRED
}
