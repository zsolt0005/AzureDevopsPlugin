package sportisimo.utils

import com.intellij.notification.Notification
import com.intellij.notification.NotificationAction
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import sportisimo.data.NotificationData
import sportisimo.states.AppSettingsState
import javax.swing.JOptionPane

/**
 * Helps work with the IntelliJ notification system.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object NotificationUtils
{
    /** The plugins notification group identification. */
    private const val NOTIFICATION_GROUP_ID = "Azure DevOps Integration"

    /** The plugins notification group identifications suffix for sticky notifications. */
    private const val NOTIFICATION_TYPE_STICKY = "STICKY"

    /** Plugin settings. */
    private val settings = AppSettingsState.getInstance()

    /** Notification type to level map. */
    private val notificationTypeToLevel: List<Pair<NotificationType, Int>> = listOf(
        NotificationType.ERROR to 1,
        NotificationType.WARNING to 2,
        NotificationType.INFORMATION to 3
    )

    private val notificationTypeToJOptionPaneType: List<Pair<NotificationType, Int>> = listOf(
        NotificationType.ERROR to JOptionPane.ERROR_MESSAGE,
        NotificationType.WARNING to JOptionPane.WARNING_MESSAGE,
        NotificationType.INFORMATION to JOptionPane.INFORMATION_MESSAGE
    )

    /**
     * Send a balloon notification to the user.
     *
     * @param title     Title.
     * @param message   Message.
     * @param type      The notification type.
     * @param isSticky  Whether the notification will be sticky or not.
     * @param action    Notifications action.
     * @param project   The project where the notification should be shown. If null, all projects sees the notification.
     * @param isAllowed If true, the notification is not shown.
     * @param asPopup   If true, the notification is shown as a popup and not a notification. If true, isSticky, action and project are not used.
     */
    fun notify(
        title: String,
        message: String,
        type: NotificationType = NotificationType.INFORMATION,
        isSticky: Boolean = false,
        action: NotificationAction? = null,
        project: Project? = null,
        isAllowed: Boolean = true,
        asPopup: Boolean = false
    ) {
        if(!isAllowed || !isAllowedByLevel(type)) return

        if(asPopup)
        {
            notifyWithPopup(title, message, type)
            return
        }

        notifyWithBalloon(title, message, type, isSticky, action, project)
    }

    fun notify(data: NotificationData)
    {
        notify(
            data.title,
            data.message,
            data.type,
            data.isSticky,
            data.action,
            data.project,
            data.isAllowed,
            data.asPopup
        )
    }

    private fun notifyWithBalloon(
        title: String,
        message: String,
        type: NotificationType,
        isSticky: Boolean = false,
        action: NotificationAction? = null,
        project: Project? = null
    )
    {
        val notificationGroupId = NOTIFICATION_GROUP_ID + if(isSticky) " $NOTIFICATION_TYPE_STICKY" else ""

        val notification = Notification(
            NotificationGroupManager.getInstance().getNotificationGroup(notificationGroupId).displayId,
            message,
            type
        )

        notification.setTitle(title)

        if (action != null)
        {
            notification.addAction(action)
        }

        notification.notify(project)
    }

    private fun notifyWithPopup(
        title: String,
        message: String,
        type: NotificationType
    )
    {
        val popupType = notificationTypeToJOptionPaneType.find { it.first == type }?.second ?: JOptionPane.INFORMATION_MESSAGE

        JOptionPane.showMessageDialog(
            null,
            message,
            title,
            popupType
        )
    }

    /**
     * Check if the type is allowed by the users current settings level.
     *
     * @param type The type to be checked.
     * @return True if is allowed, false otherwise.
     */
    private fun isAllowedByLevel(type: NotificationType): Boolean
    {
        val typeLevel = notificationTypeToLevel.find { it.first == type } ?: return false
        return settings.notificationStates.notificationLevel >= typeLevel.second
    }
}