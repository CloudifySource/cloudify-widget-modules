package cloudify.widget.hp;

import cloudify.widget.api.clouds.CloudServer;
import cloudify.widget.api.clouds.ServerIp;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 7/1/14
 * Time: 12:29 PM
 */
public class CloudServerPojo implements CloudServer {

    public String id;
    public String name;
    public ServerIp ip;
    public boolean running;
    public boolean stopped;
    public String status;



    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public ServerIp getServerIp() {
        return ip;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIp(ServerIp ip) {
        this.ip = ip;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CloudServerPojo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", ip=" + ip +
                ", running=" + running +
                ", stopped=" + stopped +
                ", status='" + status + '\'' +
                '}';
    }
}
