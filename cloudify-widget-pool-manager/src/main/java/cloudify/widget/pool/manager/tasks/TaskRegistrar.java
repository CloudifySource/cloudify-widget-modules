package cloudify.widget.pool.manager.tasks;

import cloudify.widget.mailer.Mail;
import cloudify.widget.mailer.Mailer;
import cloudify.widget.pool.manager.ErrorsDao;
import cloudify.widget.pool.manager.ITasksDao;
import cloudify.widget.pool.manager.dto.*;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Handles registration of tasks to the running-tasks data layer.
 * <p/>
 * A decorator is used to
 * perform registration before the decorated task {@code call()} is executed, and un-registration after
 * it is executed.
 */
public class TaskRegistrar {

    private static Logger logger = LoggerFactory.getLogger(TaskRegistrar.class);

    /**
     * Decorates tasks with data registration behavior.
     *
     * @param <C> The task config type.
     * @param <R> The expected callable response type.
     */
    public static abstract class TaskDecorator<C extends TaskConfig, R> implements Task<C, R> {

        protected abstract void registerTask();

        protected abstract void unregisterTask();

        public abstract void setTasksDao(ITasksDao tasksDao);
    }

    /**
     * A decorator which facilitates a DAO for data registration.
     *
     * @param <C> The task config type.
     * @param <R> The expected callable response type.
     * @see cloudify.widget.pool.manager.tasks.TaskRegistrar.TaskDecorator
     */
    public static class TaskDecoratorImpl<C extends TaskConfig, R> extends TaskDecorator<C, R> {

        Task<C, R> _decorated;

        private C _taskConfig;

        private PoolSettings _poolSettings;

        private TaskModel _taskModel;

        private ITasksDao _tasksDao;

        public TaskDecoratorImpl(Task<C, R> decorated) {
            _decorated = decorated;
        }

        @Override
        protected void registerTask() {
            _taskModel = new TaskModel()
                    .setTaskName(_decorated.getTaskName())
                    .setPoolId(_poolSettings.getUuid());
            NodeModel nodeModel = getNodeModel();
            if (nodeModel != null) {
                _taskModel.setNodeId(nodeModel.id);
            }
            _tasksDao.create(_taskModel);
        }

        private NodeModel getNodeModel() {
            if (_taskConfig instanceof NodeModelProvider) {
                return ((NodeModelProvider) _taskConfig).getNodeModel();
            }
            return null;
        }

        private String getMachineId() {
            NodeModel nodeModel = getNodeModel();
            if (nodeModel != null) {
                return nodeModel.machineId;
            }
            return null;
        }

        @Override
        protected void unregisterTask() {
            _tasksDao.delete(_taskModel.id);
        }

        @Override
        public void setTasksDao(ITasksDao tasksDao) {
            _tasksDao = tasksDao;
        }

        @Override
        public R call() throws Exception {
            logger.info("calling task with machine id [{}]", getMachineId());
            registerTask();
            try {
                R call = _decorated.call();
                unregisterTask();

                return call;
            } catch (Exception e) {
                logger.error(String.format("task [%s] failed", _decorated.getClass()), e);
                unregisterTask();
                throw e;
            }
        }

        @Override
        public TaskName getTaskName() {
            return _decorated.getTaskName();
        }

        @Override
        public void setPoolSettings(PoolSettings poolSettings) {
            _poolSettings = poolSettings;
            _decorated.setPoolSettings(poolSettings);
        }

        @Override
        public void setTaskConfig(C taskConfig) {
            _taskConfig = taskConfig;
            _decorated.setTaskConfig(taskConfig);
        }
    }


    public static abstract class TaskCallbackDecorator<R> implements TaskCallback<R> {

        protected abstract void registerError(Throwable thrown);

        public abstract void setErrorsDao(ErrorsDao errorsDao);

        public abstract void setPoolSettings(PoolSettings poolSettings);

        public abstract void setTaskName(TaskName taskName);

        public abstract void setMailer(Mailer mailer);
    }

    public static class TaskCallbackDecoratorImpl<R> extends TaskCallbackDecorator<R> {

        private TaskCallback<R> _decorated;

        private ErrorsDao _errorsDao;

        private PoolSettings _poolSettings;

        private TaskName _taskName;

        private Mailer _mailer;

        public TaskCallbackDecoratorImpl(TaskCallback<R> decorated) {
            _decorated = decorated;
        }

        @Override
        protected void registerError(Throwable thrown) {
            HashMap<String, Object> infoMap = Maps.newHashMap();
            infoMap.put("stackTrace", ExceptionUtils.getStackTrace(thrown));

            // todo - guy -  perhaps we should define an interface "ExceptionWithInfo" and function with signature "populateInfoMap(map)" to make it generic..
            if ( thrown instanceof BootstrapMachineScriptExecutionException ){
                BootstrapMachineScriptExecutionException be = (BootstrapMachineScriptExecutionException) thrown;
                infoMap.put("output", be.getOutput());
            }

            ErrorModel errorModel = new ErrorModel()
                    .setSource(_taskName.name())
                    .setMessage(thrown.getMessage())
                    .setInfoFromMap(infoMap)
                    .setPoolId(_poolSettings.getUuid());

            _errorsDao.create(errorModel);

            //send email.
            sendEmail(errorModel);
        }

        private void sendEmail(ErrorModel errorModel) {
            EmailSettings emailSettings = _poolSettings.getNodeManagement().getEmailSettings();

            if (emailSettings != null && emailSettings.isTurnedOn()) {
                Mail mail = new Mail.MailBuilder(_mailer.getMailerConfig().getFrom(), emailSettings.getRecipients(), errorModel.message)
                        .message(errorModel.toEmailString())
                        .build();
                _mailer.send(mail);
            }

        }

        @Override
        public void onSuccess(R result) {
            _decorated.onSuccess(result);
        }

        @Override
        public void onFailure(Throwable t) {
            _decorated.onFailure(t);
            registerError(t);
        }

        @Override
        public void setErrorsDao(ErrorsDao errorsDao) {
            _errorsDao = errorsDao;
        }

        public void setPoolSettings(PoolSettings poolSettings) {
            _poolSettings = poolSettings;
        }

        @Override
        public void setTaskName(TaskName taskName) {
            _taskName = taskName;
        }

        @Override
        public void setMailer(Mailer mailer) {
            _mailer = mailer;
        }
    }
}