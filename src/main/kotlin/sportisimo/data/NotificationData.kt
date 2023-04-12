package sportisimo.data

import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project

data class NotificationData(
    val title: String,
    var message: String,
    val type: NotificationType = NotificationType.INFORMATION,
    val isSticky: Boolean = false,
    val action: NotificationAction? = null,
    val project: Project? = null,
    val isAllowed: Boolean = true,
    val asPopup: Boolean = false
)