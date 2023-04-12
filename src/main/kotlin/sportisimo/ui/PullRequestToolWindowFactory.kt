package sportisimo.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import icons.CustomIcons
import sportisimo.azure.Connection
import sportisimo.events.Events
import sportisimo.events.listeners.IOnUpdatePullRequestToolWindowListener
import sportisimo.services.DataProviderService
import sportisimo.threading.ThreadingManager
import sportisimo.ui.tabs.toolwindow.PullRequestChangedFilesTab
import sportisimo.ui.tabs.toolwindow.PullRequestInfoTab
import sportisimo.ui.tabs.toolwindow.PullRequestThreadsTab
import sportisimo.utils.ToolWindowContentUtils

object PullRequestToolWindowFactory
{
    const val id = "Azure DevOps Pull Request"

    fun createToolWindow(project: Project, connection: Connection)
    {
        showToolWindow(project, connection)
    }

    fun removeIfExists(project: Project)
    {
        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            val toolWindowManager = ToolWindowManager.getInstance(project)
            val toolWindow = toolWindowManager.getToolWindow(id) ?: return@executeOnDispatchThreadAndAwaitResult

            toolWindow.remove()
        }
    }

    private fun showToolWindow(project: Project, connection: Connection)
    {
        removeIfExists(project)

        val toolWindowManager = ToolWindowManager.getInstance(project)
        val toolWindow = toolWindowManager.registerToolWindow(RegisterToolWindowTask.notClosable(
            id,
            CustomIcons.AzureRepos,
            ToolWindowAnchor.RIGHT
        ))

        val refreshToolWindowAction = object : AnAction(
            "Refresh",
            "Refresh",
            AllIcons.Actions.Refresh
        )
        {
            override fun actionPerformed(e: AnActionEvent)
            {
                BackgroundTaskUtil.syncPublisher(project, Events.ON_UPDATE_PULL_REQUEST_TOOL_WINDOW).onChange()
            }
        }

        toolWindow.setTitleActions(listOf(refreshToolWindowAction))

        toolWindow.show()

        registerEvents(project)

        prepareToolWindowPanels(project, connection, toolWindow)
    }

    private fun registerEvents(project: Project)
    {
        Events.subscribe(project, "PullRequestToolWindowFactory", Events.ON_UPDATE_PULL_REQUEST_TOOL_WINDOW, object: IOnUpdatePullRequestToolWindowListener {
            override fun onChange()
            {
                onUpdateToolWindow(project)
            }
        })
    }

    private fun prepareToolWindowPanels(
        project: Project,
        connection: Connection,
        toolWindow: ToolWindow
    )
    {
        ToolWindowContentUtils.applyNewToolWindowContentWithTabs(toolWindow,
            listOf(
                PullRequestInfoTab(project, connection),
                PullRequestChangedFilesTab(project),
                PullRequestThreadsTab(project, connection),
            )
        )
    }

    private fun onUpdateToolWindow(project: Project)
    {
        val service = project.service<DataProviderService>()
        service.getPullRequestsAsync(true)
    }
}