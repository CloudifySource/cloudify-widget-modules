package cloudify.widget.api.clouds;


import java.util.Collection;

/**
 * @author evgenyf
 *         Date: 10/7/13
 */
public interface CloudServerApi {

    /**
     * Looks up machines by the specific mask.
     * If mask is null, it will return all machines.
     * A mask is an identifier of machines of a specific pool.
     *
     * @param mask A mask to match against when searching for machines.
     * @return Machines from the pool matching the mask, or all machines, if {@code mask} is null.
     */
    public Collection<CloudServer> listByMask(String mask);

    /**
     * get CloudServer by id
     *
     * @param serverId - the server id
     * @return CloudServer - null if does not exists. otherwise CloudServer from the cloud.
     */
    public CloudServer get(String serverId);

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
    public Collection<? extends CloudServerCreated> create(MachineOptions machineOpts);

    /**
     * returns a PEM file content
     *
     * @return
     */
    public String createCertificate();


    public void connect(IConnectDetails connectDetails);

    /**
     * setter for connect details.
     * important for spring beans.
     *
     * @return
     */
    public void setConnectDetails(IConnectDetails connectDetails);

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


    public CloudExecResponse runScriptOnMachine(String script, ISshDetails sshDetails);
}
