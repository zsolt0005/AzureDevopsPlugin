package sportisimo.data.threading

import sportisimo.data.NotificationData

data class TaskData<T>(
    val retryCount: Int,
    val retryDelay: Long,
    val errorMessage: NotificationData?,
    val successMessage: NotificationData?,
    val includeExceptionMessageInError: Boolean = false,
    val callback: () -> T?
)
{
    private var currentRetryCount: Int = 0

    fun addRetry() = currentRetryCount++

    fun canRetry() = currentRetryCount <= retryCount

    fun getCurrentRetryDelay(): Long = if(currentRetryCount == 0) 0 else retryDelay + (currentRetryCount * 2)

}
