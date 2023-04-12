package sportisimo.threading

import com.intellij.openapi.application.ApplicationManager
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

/**
 * Notifies the given callbacks if the Future of the given task is finished.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 *
 * @param T The returned value type on success
 * @property future The future to be awaited
 */
class FutureNotice<T>(private val future: Future<T>)
{
    /** Registered success callback. */
    private var onSuccessCallback: ((value: T) -> Unit)? = null

    /** Registered failed callback. */
    private var onFailedCallback: ((e: Throwable) -> Unit)? = null

    /** Registered cancelled callback. */
    private var onCancelledCallback: (() -> Unit)? = null

    /** Registered exception callback. */
    private var onExceptionCallback: ((e: Throwable) -> Unit)? = null

    /**
     * Awaits the completion of the task. Depending on the tasks state, calls the corresponding registered callback.
     */
    fun awaitCompletion()
    {
        ApplicationManager.getApplication().executeOnPooledThread {
            awaitCompletionAndGetResult()
        }
    }

    fun awaitCompletionAndGetResult(): T?
    {
        try
        {
            val result = future.get()
            onSuccessCallback?.let { it(result) }

            return result
        }
        catch (e: CancellationException) // if the computation was cancelled
        {
            onCancelledCallback?.let { it() }
            onFailedCallback?.let { it(e) }
        }
        catch (e: ExecutionException) // if the computation threw an exception
        {
            onExceptionCallback?.let { it(e) }
            onFailedCallback?.let { it(e) }
        }
        catch (e: InterruptedException) // if the current thread was interrupted while waiting
        {
            onExceptionCallback?.let { it(e) }
            onFailedCallback?.let { it(e) }
        }

        return null
    }

    /**
     * Register a function that will be run if the executions of the task completes. The task result is passed to the function.
     *
     * @param cb The function to be called.
     * @return self
     */
    fun onSuccess(cb: (value: T) -> Unit): FutureNotice<T>
    {
        onSuccessCallback = cb
        return this
    }

    /**
     * Register a function that will be run if the executions of the task returns an exception or is cancelled.
     *
     * @param cb The function to be called.
     * @return self
     */
    fun onFailed(cb: (e: Throwable) -> Unit): FutureNotice<T>
    {
        onFailedCallback = cb
        return this
    }

    /**
     * Register a function that will be run if the executions of the task is cancelled.
     *
     * @param cb The function to be called.
     * @return self
     */
    fun onCancelled(cb: () -> Unit) : FutureNotice<T>
    {
        onCancelledCallback = cb
        return this
    }

    /**
     * Register a function that will be run if the executions of the task returns an exception.
     *
     * @param cb The function to be called.
     * @return self
     */
    fun onException(cb: (e: Throwable)  -> Unit) : FutureNotice<T>
    {
        onExceptionCallback = cb
        return this
    }
}