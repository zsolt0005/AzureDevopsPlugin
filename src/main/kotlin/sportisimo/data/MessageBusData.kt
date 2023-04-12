package sportisimo.data

import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic

data class MessageBusData(
    val project: Project,
    val key: Any,
    val topic: Topic<*>,
    val connection: MessageBusConnection
)