package cloudify.widget.pool.manager.dto;

import cloudify.widget.pool.manager.tasks.TaskName;

/**
 * User: eliranm
 * Date: 3/19/14
 * Time: 12:34 PM
 */
public class TaskModel {

    public static final int INITIAL_ID = -1;

    public long id = INITIAL_ID;
    public TaskName taskName;
    public long nodeId;
    public long startTime = System.currentTimeMillis();
    public String poolId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TaskName getTaskName() {
        return taskName;
    }

    public TaskModel setTaskName(TaskName taskName) {
        this.taskName = taskName;
        return this;
    }

    public long getNodeId() {
        return nodeId;
    }

    public TaskModel setNodeId(long nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getPoolId() {
        return poolId;
    }

    public TaskModel setPoolId(String poolId) {
        this.poolId = poolId;
        return this;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "TaskModel{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", poolId='" + poolId + '\'' +
                ", nodeId='" + nodeId + '\'' +
                '}';
    }
}
