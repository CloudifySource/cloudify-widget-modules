package cloudify.widget.pool.manager;

import cloudify.widget.common.CollectionUtils;
import cloudify.widget.pool.manager.dto.TaskModel;

import org.apache.commons.collections.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created with IntelliJ IDEA.
 * User: guym
 * Date: 3/20/14
 * Time: 12:46 PM
 */
public class InMemoryTasksDao implements ITasksDao {

    private static Logger logger = LoggerFactory.getLogger(InMemoryTasksDao.class);

    List<TaskModel> tasks = new LinkedList<TaskModel>();
    AtomicLong idCounter = new AtomicLong(0);

    @Override
    public List<TaskModel> readAllOfPool(final String poolId) {
        logger.trace("getting all tasks for pool [{}]", poolId);
        Collection<TaskModel> result =  (Collection<TaskModel>) CollectionUtils.select( tasks, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                try{
                 return ((TaskModel)object).getPoolId().equals(poolId);
                }catch(Exception e){
                    logger.error("error while selecting tasks for pool",e);
                    return false;
                }
            }
        });
        return new LinkedList<TaskModel>(result);
    }

    @Override
    public TaskModel read(final long taskId) {
        logger.info("reading task [{}]", taskId);
        return (TaskModel) CollectionUtils.find(tasks, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((TaskModel) object).getId() == taskId;

            }
        });

    }

    @Override
    public boolean create(TaskModel taskModel) {
        taskModel.setId( idCounter.incrementAndGet());
        logger.info("creating task [{}]", taskModel.getId());
        tasks.add(taskModel);
        return true;
    }

    @Override
    public int delete(long taskId) {
        logger.info("deleting task " + taskId);
        tasks.remove( read(taskId));
        return 1;
    }

    @Override
    public int update(TaskModel taskModel) {
        return 1;
    }


}
