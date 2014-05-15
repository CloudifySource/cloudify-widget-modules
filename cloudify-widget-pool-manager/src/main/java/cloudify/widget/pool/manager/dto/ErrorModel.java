package cloudify.widget.pool.manager.dto;

import cloudify.widget.pool.manager.tasks.TaskName;
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
    public TaskName taskName = TaskName.UNKNOWN;
    public String poolId;
    public String message;
    public String info;

    public ErrorModel setId(long id) {
        this.id = id;
        return this;
    }

    public ErrorModel setTaskName(TaskName taskName) {
        this.taskName = taskName;
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

    @Override
    public String toString() {
        return "TaskErrorModel{" +
                "id=" + id +
                ", taskName=" + taskName +
                ", poolId='" + poolId + '\'' +
                ", message='" + message + '\'' +
                ", info='" + info + '\'' +
                '}';
    }
}
