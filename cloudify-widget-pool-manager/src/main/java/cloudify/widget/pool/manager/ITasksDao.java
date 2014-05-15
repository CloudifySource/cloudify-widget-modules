package cloudify.widget.pool.manager;

import cloudify.widget.pool.manager.dto.TaskModel;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/20/14
 * Time: 12:46 PM
 */
public interface ITasksDao {
    public List<TaskModel> readAllOfPool(String poolId);

    public TaskModel read(long taskId);

    public boolean create(TaskModel taskModel);

    public int delete(long taskId);

    public int update(TaskModel taskModel);
}
