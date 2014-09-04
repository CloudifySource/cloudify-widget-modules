package cloudify.widget.pool.manager;

/**
 * Created by sefi on 9/3/14.
 */
public class ThreadPoolStatus {
    private int activeThreads = -1;

    private long completedTaskCount = -1;
    private long taskCount = -1;

    private int corePoolSize = -1;
    private int largestPoolSize = -1;
    private int maximumPoolSize = -1;
    private int currentPoolSize = -1;

    /**
     * @return  Returns the approximate number of threads that are actively executing tasks.
     */
    public int getActiveThreads() {
        return activeThreads;
    }

    public void setActiveThreads(int activeThreads) {
        this.activeThreads = activeThreads;
    }

    /**
     *
     * @return Returns the approximate total number of tasks that have completed execution.
     */
    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    public void setCompletedTaskCount(long completedTaskCount) {
        this.completedTaskCount = completedTaskCount;
    }

    /**
     *
     * @return     Returns the approximate total number of tasks that have ever been scheduled for execution.
     */
    public long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(long taskCount) {
        this.taskCount = taskCount;
    }

    /**
     *
     * @return Returns the core number of threads.
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    /**
     *
     * @return  Returns the largest number of threads that have ever simultaneously been in the pool.
     */
    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    public void setLargestPoolSize(int largestPoolSize) {
        this.largestPoolSize = largestPoolSize;
    }

    /**
     *
     * @return  Returns the maximum allowed number of threads.
     */
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    public void setMaximumPoolSize(int maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    /**
     *
     * @return  Returns the current number of threads in the pool.
     */
    public int getCurrentPoolSize() {
        return currentPoolSize;
    }

    public void setCurrentPoolSize(int currentPoolSize) {
        this.currentPoolSize = currentPoolSize;
    }

}
