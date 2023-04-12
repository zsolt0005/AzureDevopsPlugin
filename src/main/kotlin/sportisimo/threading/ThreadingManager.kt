package sportisimo.threading

import com.intellij.openapi.application.ApplicationManager
import sportisimo.data.threading.RunningTaskData
import sportisimo.data.threading.TaskData
import sportisimo.utils.NotificationUtils
import java.awt.EventQueue
import java.util.concurrent.Future

/**
 * Working with multithreaded tasks.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
class ThreadingManager
{
    /** List of currently running tasks. */
    private val runningTasks = mutableMapOf<String, RunningTaskData<*>>()

    /**
     * Executes the callback on a pooled thread. Allows only single run for a specific key.
     * If called multiple times with the same key, the running task with the key will be terminated, and the new one will run.
     *
     * @param taskKey Key of the task.
     * @param cb      Callback to be run.
     * @return the Future of the task.
     */
    fun <T>executeOnPooledThread(taskKey: String, cb: () -> T): Future<T>
    {
        checkKey(taskKey)

        val task = executeOnPooledThread(cb)

        runningTasks[taskKey] = task

        return task.task!!
    }

    fun <T>executeOnPooledThreadAndRetryOnFail(taskKey: String, data: TaskData<T>): Future<T>
    {
        checkKey(taskKey)

        val task = executeOnPooledThreadAndRetryOnFail(data)

        runningTasks[taskKey] = task

        return task.task!!
    }

    private fun checkKey(taskKey: String)
    {
        if(!runningTasks.keys.contains(taskKey)) return

        cancelTask(runningTasks[taskKey]!!)

        runningTasks.remove(taskKey)
    }

    companion object
    {
        /**
         * Executes the callback on a pooled thread.
         *
         * @param cb Callback to be run.
         * @return the Future of the task.
         */
        fun <T>executeOnPooledThread(cb: () -> T?): RunningTaskData<T>
        {
            val runningTaskData = RunningTaskData<T>()

            runningTaskData.task = ApplicationManager.getApplication().executeOnPooledThread<T> InternalExecutor@ {
                try
                {
                    cb()
                }
                catch (_: InterruptedException) { return@InternalExecutor null }
            }

            return runningTaskData
        }

        fun <T>executeOnPooledThreadAndRetryOnFail(data: TaskData<T>, runningTaskData: RunningTaskData<T> = RunningTaskData()): RunningTaskData<T>
        {
            if(runningTaskData.cancelled)
            {
                cancelTask(runningTaskData)
                return runningTaskData
            }

            Thread.sleep(data.getCurrentRetryDelay() * 1000)

            runningTaskData.task = ApplicationManager.getApplication().executeOnPooledThread<T> {
                var result: T? = null
                runCatching {
                    result = data.callback()
                }
                    .onFailure {
                        if(it is InterruptedException) return@onFailure

                        data.addRetry()

                        if(data.errorMessage != null)
                        {
                            val errorMessage = data.errorMessage.copy()

                            if(data.includeExceptionMessageInError)
                            {
                                if(errorMessage.message.isNotEmpty()) errorMessage.message += "<br>"
                                errorMessage.message += "<span style='color: orange;'>${it.message}</span>"
                            }

                            val retryMessage =
                                if(data.canRetry()) "Retry in ${data.getCurrentRetryDelay()} seconds."
                                else ""

                            errorMessage.message += "<br><br>$retryMessage"

                            NotificationUtils.notify(errorMessage)
                        }

                        if(!data.canRetry()) return@onFailure

                        executeOnPooledThreadAndRetryOnFail(data, runningTaskData)
                    }
                    .onSuccess {
                        if(data.successMessage == null) return@onSuccess

                        NotificationUtils.notify(data.successMessage)
                    }

                return@executeOnPooledThread result
            }

            return runningTaskData
        }

        fun <T> executeOnDispatchThreadAndAwaitResult(cb: () -> T): T?
        {
            if(EventQueue.isDispatchThread())
            {
                return cb()
            }

            var result: T? = null
            EventQueue.invokeAndWait {
                result = cb()
            }
            return result
        }

        fun <T> executeOnDispatchThread(cb: () -> T)
        {
            if(EventQueue.isDispatchThread()) {
                cb()
                return
            }

            EventQueue.invokeLater {
                cb()
            }
        }

        fun awaitAll(runningTasks: List<Future<*>>) = executeOnPooledThread {
            runCatching {
                runningTasks.forEach { it.get() }
            }
        }

        private fun <T>cancelTask(runningTaskData: RunningTaskData<T>)
        {
            runningTaskData.cancelled = true

            if(runningTaskData.task == null || runningTaskData.task!!.isDone) return

            runningTaskData.task!!.cancel(true)
        }
    }
}

// AppExecutorUtil.getAppScheduledExecutorService().schedule