package sportisimo.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener
import sportisimo.azure.Connection
import sportisimo.azure.DevOpsHelper
import sportisimo.azure.GitHelper
import sportisimo.data.ConnectionData
import sportisimo.data.GitRepositoryData
import sportisimo.data.NotificationData
import sportisimo.data.azure.RepositoryData
import sportisimo.data.azure.RepositoryRefData
import sportisimo.data.threading.TaskData
import sportisimo.events.Events
import sportisimo.events.listeners.ConnectionChangedListener
import sportisimo.events.listeners.IOnConnectionSelectedManuallyListener
import sportisimo.events.listeners.IOnUpdateDevopsToolWindowListener
import sportisimo.exceptions.AbortException
import sportisimo.exceptions.NotFoundException
import sportisimo.services.DataProviderService
import sportisimo.states.AppSettingsState
import sportisimo.states.ProjectCache
import sportisimo.states.ProjectDataState
import sportisimo.threading.FutureNotice
import sportisimo.threading.ThreadingManager
import sportisimo.ui.tabs.toolwindow.RepoInfoTab
import sportisimo.utils.NotificationUtils
import sportisimo.utils.ToolWindowContentUtils
import javax.swing.JOptionPane

class DevOpsToolWindowFactory: ToolWindowFactory
{
    private val settings = AppSettingsState.getInstance()
    private val threadingManager = ThreadingManager()
    private lateinit var cachedData: ProjectDataState
    private lateinit var projectCache: ProjectCache

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow)
    {
        DumbService.getInstance(project).runWhenSmart {
            cachedData = ProjectDataState.getInstance(project)
            projectCache = ProjectCache.getInstance(project)
            ToolWindowContentUtils.createLoadingWindow(toolWindow)
            setupToolWindow(project, toolWindow)
        }
    }

    private fun setupToolWindow(project: Project, toolWindow: ToolWindow)
    {
        val addNewConnectionAction = object : AnAction(
            "Add New Connection",
            "Opens a window where a new connection can be established",
            AllIcons.General.Add
        ) {
            override fun actionPerformed(e: AnActionEvent)
            {
                AddConnectionWindow().show()
            }
        }

        val selectConnectionManuallyAction = object : AnAction(
            "Select Connection Manually",
            "Select connection, repository and branch manually",
            AllIcons.Actions.Edit
        ) {
            override fun actionPerformed(e: AnActionEvent)
            {
                SelectConnectionManuallyWindow(project).show()
            }
        }

        val refreshToolWindowAction = object : AnAction(
            "Refresh",
            "Hard refresh all data",
            AllIcons.Actions.Refresh
        ) {
            override fun actionPerformed(e: AnActionEvent)
            {
                loadSettings(project, toolWindow)
            }
        }

        val removeToolWindowAction = object : AnAction(
            "Remove Cache",
            "Removes the saved configuration file",
            AllIcons.Actions.GC
        ) {
            override fun actionPerformed(e: AnActionEvent)
            {
                val confirmation = JOptionPane.showConfirmDialog(
                    null,
                    "Delete all cached data?",
                    "Delete Cache",
                    JOptionPane.YES_NO_OPTION
                )

                if (confirmation != JOptionPane.YES_OPTION) return

                cachedData.clearData()
                projectCache.clearData()

                PullRequestToolWindowFactory.removeIfExists(project)
                loadSettings(project, toolWindow)
            }
        }

        toolWindow.setTitleActions(listOf(
            addNewConnectionAction,
            selectConnectionManuallyAction,
            refreshToolWindowAction,
            removeToolWindowAction
        ))

        registerEvents(project, toolWindow)

        // Git change listener
        Events.subscribe(project, "DevOpsToolWindowFactory", GitRepository.GIT_REPO_CHANGE, GitRepositoryChangeListener {
            reloadToolWindow(project, toolWindow)
        })

        loadSettings(project, toolWindow)
    }

    private fun registerEvents(project: Project, toolWindow: ToolWindow)
    {
        val eventId = "DevOpsToolWindowFactory.setupToolWindow"
        Events.Application.OnConnectionAdded.register(project, eventId, ConnectionChangedListener(project) { reloadToolWindow(project, toolWindow) })
        Events.Application.OnConnectionRemoved.register(project, eventId, ConnectionChangedListener(project) { reloadToolWindow(project, toolWindow) })

        Events.subscribe(project, "DevOpsToolWindowFactory", Events.ON_CONNECTION_SELECTED_MANUALLY, object: IOnConnectionSelectedManuallyListener {
            override fun onChange()
            {
                reloadToolWindow(project, toolWindow, true)
            }
        })
        Events.subscribe(project, "DevOpsToolWindowFactory", Events.ON_UPDATE_DEVOPS_TOOL_WINDOW, object: IOnUpdateDevopsToolWindowListener {
            override fun onChange()
            {
                reloadToolWindow(project, toolWindow, true)
            }
        })
    }

    private fun reloadToolWindow(project: Project, toolWindow: ToolWindow, isForced: Boolean = false)
    {
        val localGitData = getLocalGitData(project, toolWindow)
        val connectionStillExists = DevOpsHelper.connectionStillExists(cachedData)

        if(
            !isForced
            && connectionStillExists
            && localGitData.repositoryName == cachedData.gitData?.repositoryName
            && localGitData.branchName == cachedData.gitData?.branchName
        )
        {
            // No need for update
            return
        }

        PullRequestToolWindowFactory.removeIfExists(project)
        loadSettings(project, toolWindow)
    }

    private fun loadSettings(project: Project, toolWindow: ToolWindow)
    {
        ToolWindowContentUtils.createLoadingWindow(toolWindow)

        val taskData = TaskData(
            3, // TODO Hard coded value
            5, // TODO Hard coded value
            NotificationData("Failed to load all the necessary data!", ""),
            null,
            true
        )
        {
            try
            {
                tryLoadSettings(project, toolWindow)
            }
            catch (_: AbortException) {}
        }

        threadingManager.executeOnPooledThreadAndRetryOnFail("loadSettings", taskData)
    }

    private fun createConnectionFoundWindow(toolWindow: ToolWindow, project: Project, connection: Connection)
    {
        ToolWindowContentUtils.applyNewToolWindowContentWithTabs(toolWindow,
            listOf(
                RepoInfoTab(project, connection)
            )
        )
    }

    private fun tryLoadSettings(project: Project, toolWindow: ToolWindow)
    {
        checkSelectedManuallyStillExists(project)

        // Detect if the repository is a DevOps remote
        val isDevOpsTask = threadingManager.executeOnPooledThread("checkIsDevOpsRemote") { checkIsDevOpsRemote(project) }
        val isDevOpsRemote = FutureNotice(isDevOpsTask).awaitCompletionAndGetResult() ?: false
        if (!isDevOpsRemote && !cachedData.selectedManually)
        {
            ToolWindowContentUtils.createErrorWindow(
                toolWindow,
                "<b>Current</b> repository is <b>not</b> a <b>DevOps</b> remote."
            )
            PullRequestToolWindowFactory.removeIfExists(project)
            return
        }

        // Get fresh local git data
        cachedData.gitData = getLocalGitData(project, toolWindow)

        if(!cachedData.selectedManually)
        {
            cachedData.connectionData = getPossibleConnection(project, toolWindow)
            cachedData.repositoryData = getRepository(project, toolWindow)
            cachedData.branchData = getBranchData(project, toolWindow)
        }

        val connection = Connection(cachedData.connectionData!!.organization, cachedData.connectionData!!.token)
        val service = project.service<DataProviderService>()

        service.getWorkItemTypesAsync(true)
        service.getCurrentUserAsync(true)

        createConnectionFoundWindow(toolWindow, project, connection)
    }

    private fun checkSelectedManuallyStillExists(project: Project)
    {
        runCatching {
            if(cachedData.selectedManually && !DevOpsHelper.connectionStillExists(cachedData))
            {
                cachedData.clearData()
                PullRequestToolWindowFactory.removeIfExists(project)
            }

            return
        }.onFailure {
            var errorMessage = it.message ?: "Exception does not returned any error message."

            errorMessage += " Please try again. If the error persist, restart your IDE."

            NotificationUtils.notify(
                "Something went wrong while loading or removing the tool window state",
                errorMessage,
                project = project,
                isAllowed = settings.notificationStates.toolWindowInitialization
            )
        }

        throw AbortException()
    }

    /**
     * Checks if the current repository is a dev ops remote.
     *
     * @param project
     * @return true if is a dev ops remote, false otherwise.
     * @throws AbortException If the parent function scope should be terminated.
     */
    private fun checkIsDevOpsRemote(project: Project): Boolean
    {
        val isDevOpsRemote = ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            GitHelper.isRemoteDevOps(project)
        }

        if(isDevOpsRemote == null)
        {
            NotificationUtils.notify(
                "Unexpected error while detecting git remote host",
                "",
                project = project,
                isAllowed = settings.notificationStates.toolWindowInitialization
            )
            throw AbortException()
        }

        return isDevOpsRemote
    }

    /**
     * Loads the local git data.
     *
     * @param project
     * @param toolWindow
     * @throws AbortException If the parent function scope should be terminated.
     */
    private fun getLocalGitData(project: Project, toolWindow: ToolWindow): GitRepositoryData
    {
        runCatching<GitRepositoryData> {
            ThreadingManager.executeOnDispatchThreadAndAwaitResult {
                GitRepositoryData(
                    repositoryName = GitHelper.getRepositoryName(project),
                    branchName = GitHelper.getBranchName(project)
                )
            } ?: throw Exception()
        }
            .onSuccess { return it }
            .onFailure {
                val message = it.message ?: "Unexpected error happened while detecting current repository name and branch"
                NotificationUtils.notify(
                    "Failed to detect git repository",
                    message,
                    project = project,
                    isAllowed = settings.notificationStates.toolWindowInitialization
                )
                ToolWindowContentUtils.createErrorWindow(toolWindow, message)
                PullRequestToolWindowFactory.removeIfExists(project)
            }

        throw AbortException()
    }

    private fun getPossibleConnection(
        project: Project,
        toolWindow: ToolWindow
    ): ConnectionData
    {
        val possibleConnections = runCatching<List<ConnectionData>> {
            DevOpsHelper.detectDevOpsConnectionFromRepositoryName(cachedData.gitData!!.repositoryName)
        }.getOrNull()

        if (possibleConnections.isNullOrEmpty())
        {
            ToolWindowContentUtils.createErrorWindow(
                toolWindow,
                """
                    <h2>No connection found!</h2>
                    Please add a new connection or select your configuration manually.<br>
                    If your current connection has an expired token, it will trigger this error too.
                """.trimIndent()
            )
            PullRequestToolWindowFactory.removeIfExists(project)

            throw AbortException()
        }

        if (possibleConnections.size > 1)
        {
            val possibleConnectionsNames = possibleConnections.joinToString { "${it.organization} {${it.project}}" }
            val message = "Can't auto detect! Connections found: ${possibleConnections.size} -> ($possibleConnectionsNames)"

            ToolWindowContentUtils.createErrorWindow(toolWindow, message)
            PullRequestToolWindowFactory.removeIfExists(project)
            throw AbortException()
        }

        return possibleConnections.first()
    }

    private fun getRepository(
        project: Project,
        toolWindow: ToolWindow
    ): RepositoryData
    {
        runCatching {
            return DevOpsHelper.findRepositoryByRepositoryName(cachedData.connectionData!!, cachedData.gitData!!.repositoryName)
        }
            .onFailure {
                val message = it.message ?: "Unexpected error happened while fetching repository from Azure REST API"
                NotificationUtils.notify(
                    "Failed to detect repository from Azure DevOps",
                    message,
                    project = project,
                    isAllowed = settings.notificationStates.toolWindowInitialization
                )
                ToolWindowContentUtils.createErrorWindow(toolWindow, message)
                PullRequestToolWindowFactory.removeIfExists(project)
            }

        throw AbortException()
    }

    /**
     * Loads the branch data from the Azure REST API.
     *
     * @param project
     * @param toolWindow
     * @throws AbortException If the parent function scope should be terminated.
     */
    private fun getBranchData(
        project: Project,
        toolWindow: ToolWindow
    ): RepositoryRefData
    {
        try
        {
            return DevOpsHelper.getBranchFromRepositoryAndBranchName(
                cachedData.connectionData!!,
                cachedData.repositoryData!!,
                cachedData.gitData!!.branchName
            )
        }
        catch (e: NotFoundException)
        {
            val message = e.message!!
            NotificationUtils.notify(
                "Failed to detect branch from Azure DevOps",
                message,
                project = project,
                isAllowed = settings.notificationStates.toolWindowInitialization
            )
            ToolWindowContentUtils.createErrorWindow(toolWindow, message)
            PullRequestToolWindowFactory.removeIfExists(project)
        }
        catch (e: Throwable)
        {
            val message = e.message ?: "Unexpected error happened while fetching branch from Azure REST API"
            NotificationUtils.notify(
                "Failed to detect branch from Azure DevOps",
                message,
                project = project,
                isAllowed = settings.notificationStates.toolWindowInitialization
            )
            ToolWindowContentUtils.createErrorWindow(toolWindow, message)
            PullRequestToolWindowFactory.removeIfExists(project)
        }

        throw AbortException()
    }
}