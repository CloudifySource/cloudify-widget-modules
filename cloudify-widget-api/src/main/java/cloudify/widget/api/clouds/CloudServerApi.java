package cloudify.widget.api.clouds;


import java.util.Collection;

/**
 * @author evgenyf
 *         Date: 10/7/13
 */
public interface CloudServerApi<S extends CloudServer, C extends CloudServerCreated, D extends IConnectDetails, O extends MachineOptions, H extends ISshDetails> {

    /**
     * Looks up machines by the specific mask.
     * If mask is null, it will return all machines.
     * A mask is an identifier of machines of a specific pool.
     *
     * @param mask A mask to match against when searching for machines.
     * @return Machines from the pool matching the mask, or all machines, if {@code mask} is null.
     */
    public Collection<S> listByMask(String mask);

    /**
     * get CloudServer by id
     *
     * @param serverId - the server id
     * @return CloudServer - null if does not exists. otherwise CloudServer from the cloud.
     */
    public S get(String serverId);

    /**
     * Machine should be removed from the cloud
     *
     * @param id - id of node
     */
    public void delete(String id);

    /**
     * rebuild the machine
     *
     * @param id - machine id
     */
    public void rebuild(String id);

    /**
     * create a new machine
     *
     * @param machineOpts - options for machine
     * @return an instance that holds the new machine's id.
     */
    public Collection<C> create(O machineOpts);

    /**
     * returns a PEM file content
     *
     * @return
     */
    public String createCertificate();


    public void connect(D connectDetails);

    /**
     * setter for connect details.
     * important for spring beans.
     *
     * @return
     */
    public void setConnectDetails(D connectDetails);

    /**
     * uses the connect details.
     * important for spring beans.
     */
    public void connect();

    /**
     * creates a security group
     */
    public void createSecurityGroup(ISecurityGroupDetails securityGroupDetails);

    @Deprecated
    public CloudExecResponse runScriptOnMachine(String script, String serverIp);


    public CloudExecResponse runScriptOnMachine(String script, H sshDetails);
}
