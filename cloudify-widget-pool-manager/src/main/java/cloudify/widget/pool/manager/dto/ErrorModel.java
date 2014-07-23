package cloudify.widget.pool.manager.dto;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * User: eliranm
 * Date: 3/9/14
 * Time: 2:15 PM
 */
public class ErrorModel {

    public static final int INITIAL_ID = -1;

    public long id = INITIAL_ID;
    public String source = "N/A";
    public String poolId;
    public String message;
    public String info;
    public long timestamp = System.currentTimeMillis();

    public ErrorModel setId(long id) {
        this.id = id;
        return this;
    }

    public ErrorModel setSource(String source) {
        this.source = source;
        return this;
    }

    public ErrorModel setPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    public ErrorModel setMessage(String message) {
        this.message = message;
        return this;
    }

    public ErrorModel setInfo(String info) {
        this.info = info;
        return this;
    }

    /**
     * This method can't be named setInfo, or JSON mapping will fail.
     * @param info
     * @return
     */
    public ErrorModel setInfoFromMap(Map<String, Object> info) {
        try {
            this.info = new ObjectMapper().writeValueAsString(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public String toEmailString() {
        return "ErrorModel: {" +
                "\nid=" + id +
                ", \nsource='" + source + '\'' +
                ", \npoolId='" + poolId + '\'' +
                ", \nmessage='" + message + '\'' +
                ", \ninfo='" + info + '\'' +
                ", \ntimestamp=" + timestamp +
                '}';
    }

    @Override
    public String toString() {
        return "ErrorModel{" +
                "id=" + id +
                ", source='" + source + '\'' +
                ", poolId='" + poolId + '\'' +
                ", message='" + message + '\'' +
                ", info='" + info + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
