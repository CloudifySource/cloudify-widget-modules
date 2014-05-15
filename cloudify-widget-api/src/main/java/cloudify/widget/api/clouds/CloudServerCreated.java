package cloudify.widget.api.clouds;

/**
 * @author evgenyf
 *         Date: 10/7/13
 */
public interface CloudServerCreated {

    /**
     * Gets the machine id.
     *
     * @return The machine id.
     */
    String getId();

    /**
     * Provides means of accessing the machine credentials.
     * Machine credentials are available after the node is created, but are not guaranteed to be
     * persisted to the node metadata later in the node's lifecycle.
     *
     * @return A representation of the machine credentials at the time of creation.
     */
    ISshDetails getSshDetails();
}
