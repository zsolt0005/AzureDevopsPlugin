package sportisimo.events.listeners

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import sportisimo.utils.NotificationUtils

abstract class AChangeListener<T>(val project: Project)
{
    abstract fun onChange(result: T?)

    protected fun runCallback(cb: (T?) -> Unit, result: T?)
    {
        runCatching {
            cb(result)
        }.onFailure {
            NotificationUtils.notify(
                "Unexpected error",
                it.message ?: "Error while executing an event callback",
                NotificationType.ERROR,
                true,
                project = project
            )
        }
    }

    fun isDisposed() = project.isDisposed

    fun isSameProject(projectToCompare: Project): Boolean = project == projectToCompare
}