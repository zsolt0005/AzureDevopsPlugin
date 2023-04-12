package sportisimo.events

import com.intellij.openapi.project.Project
import com.intellij.util.messages.MessageBusConnection
import com.intellij.util.messages.Topic
import sportisimo.data.MessageBusData
import sportisimo.events.listeners.*

object Events
{
    private val listeners: MutableList<MessageBusData> = mutableListOf()

    object Application
    {
        object OnConnectionRemoved: AApplicationLevelEvent<Any>()
        object OnConnectionAdded: AApplicationLevelEvent<Any>()
    }

    fun <T : Any>subscribe(project: Project, key: Any, topic: Topic<T>, listener: T): MessageBusConnection
    {
        unSubscribe(project, key, topic)

        val connection = project.messageBus.connect()
        connection.subscribe(topic, listener)

        val messageBusData = MessageBusData(project, key, topic, connection)
        listeners.add(messageBusData)

        return connection
    }

    fun <T : Any>unSubscribe(project: Project, key: Any, topic: Topic<T>)
    {
        val listener = listeners.find {
            it.key == key && it.project == project && it.topic == topic
        }

        listener?.connection?.disconnect()
        listeners.remove(listener)
    }

    val ON_PULL_REQUESTS_LOADED = Topic.create("", IOnPullRequestsLoadedListener::class.java)
    val ON_PULL_REQUEST_WORK_ITEMS_LOADED = Topic.create("", IOnPullRequestWorkItemsLoadedListener::class.java)
    val ON_PULL_REQUEST_TARGET_BRANCH_COMMITS_LOADED = Topic.create("", IOnPullRequestTargetBranchCommitsLoadedListener::class.java)
    val ON_PULL_REQUEST_COMMENTS_LOADED = Topic.create("", IOnPullRequestThreadsLoadedListener::class.java)
    val ON_PULL_REQUEST_PIPELINE_RUN_LOADED = Topic.create("", IOnPullRequestPipelineRunLoadedListener::class.java)
    val ON_PROJECT_TEAMS_LOADED = Topic.create("", IOnProjectTeamsLoadedListener::class.java)
    val ON_UPDATE_PULL_REQUEST_FAILED = Topic.create("", IOnUpdatePullRequestFailedListener::class.java)
    val ON_UPDATE_PULL_REQUEST_TOOL_WINDOW = Topic.create("", IOnUpdatePullRequestToolWindowListener::class.java)
    val ON_UPDATE_DEVOPS_TOOL_WINDOW = Topic.create("", IOnUpdateDevopsToolWindowListener::class.java)
    val ON_CONNECTION_SELECTED_MANUALLY = Topic.create("", IOnConnectionSelectedManuallyListener::class.java)
}