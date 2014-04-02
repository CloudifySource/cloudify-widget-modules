package cloudify.widget.softlayer;

import cloudify.widget.api.clouds.ISshDetails;

/**
 * User: evgenyf
 * Date: 2/10/14
 */
public class SoftlayerSshDetails implements ISshDetails {

    private final int port;
    private final String user;
    private final String password;
    private final String publicIp;

    public SoftlayerSshDetails( int port, String user, String password, String publicIp ){
        this.port = port;
        this.user = user;
        this.password = password;
        this.publicIp = publicIp;
    }

    public int port(){
        return port;
    }

    public String user(){
        return user;
    }

    public String password(){
        return password;
    }

    public String publicIp(){ return publicIp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SoftlayerSshDetails that = (SoftlayerSshDetails) o;

        if (port != that.port) return false;
        if (password != null ? !password.equals(that.password) : that.password != null) return false;
        if (publicIp != null ? !publicIp.equals(that.publicIp) : that.publicIp != null) return false;
        if (user != null ? !user.equals(that.user) : that.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (publicIp != null ? publicIp.hashCode() : 0);
        return result;
    }
}