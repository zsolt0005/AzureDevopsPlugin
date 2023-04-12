package sportisimo.ui.tabs.toolwindow

import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import sportisimo.azure.Connection
import sportisimo.data.LastOpenedPullRequestData
import sportisimo.data.azure.ProjectTeamData
import sportisimo.data.azure.PullRequestData
import sportisimo.events.Events
import sportisimo.events.listeners.IOnProjectTeamsLoadedListener
import sportisimo.events.listeners.IOnPullRequestsLoadedListener
import sportisimo.services.DataProviderService
import sportisimo.states.AppSettingsState
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.CreatePullRequestWindow
import sportisimo.ui.PullRequestToolWindowFactory
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.HtmlUtils
import sportisimo.utils.NotificationUtils
import sportisimo.utils.StringUtils
import sportisimo.utils.UIUtils
import java.awt.Color
import javax.swing.JPanel

class RepoInfoTab(
    private val project: Project,
    private val connection: Connection
): ATab()
{
    private val settings = AppSettingsState.getInstance()
    private val cachedData = ProjectDataState.getInstance(project)
    private val service = project.service<DataProviderService>()

    private lateinit var projectTeamsContentPanel: JPanel
    private lateinit var pullRequestContentPanel: JPanel

    override fun getPanel(): JPanel
    {
        projectTeamsContentPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        pullRequestContentPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())

        val connectionData = cachedData.connectionData!!

        val gitData = cachedData.gitData!!
        val repositoryData = cachedData.repositoryData!!
        val branchData = cachedData.branchData!!

        val panel = panel {
            row { text(HtmlUtils.boldText(connectionData.project.name, 2.0f)).horizontalAlign(HorizontalAlign.CENTER) }
            row { text(HtmlUtils.boldText(connectionData.organization, 1.4f)).horizontalAlign(HorizontalAlign.CENTER) }

            indent {
                group("Teams", true) { row { cell(projectTeamsContentPanel) } }

                group("Pull Requests", true) { row { cell(pullRequestContentPanel) } }

                group("Azure Repo", true) {
                    row { text("<b>Repository</b>")}
                    indent {
                        twoColumnsRow(
                            { label("Name:") },
                            {
                                label(repositoryData.name)
                                rowComment("(${repositoryData.id})")
                            }
                        )
                        twoColumnsRow(
                            {label("Url:")},
                            {link("Open in browser") { BrowserUtil.browse(repositoryData.webUrl)} }
                        )
                    }

                    row { text("<b>Branch</b>")}
                    indent {
                        twoColumnsRow(
                            {label("Name:")},
                            {
                                label(StringUtils.cleanRepositoryRefName(branchData.name))
                                rowComment("(${branchData.objectId})")
                            }
                        )
                        twoColumnsRow(
                            {label("Owner name:")}
                        ) {
                            runCatching {
                                val avatarImage = service.getAvatar(branchData.creator.imageUrl)
                                icon(avatarImage)
                            }

                            label(branchData.creator.displayName)
                        }
                    }
                }

                group("Local Git", true) {
                    twoColumnsRow(
                        {label("Repository name:")},
                        {label(gitData.repositoryName)}
                    )
                    twoColumnsRow(
                        {label("Branch name:")},
                        {label(gitData.branchName)}
                    )
                }
            }
        }

        registerEvents()

        service.getProjectTeamsAsync(true, onFailed = {
            loadProjectTeamsDataFailed(it)
        })
        service.getPullRequestsAsync(true)

        return UIUtils.createScrollablePanel(panel)
    }

    private fun registerEvents()
    {
        Events.subscribe(project, "RepoInfoTab", Events.ON_PROJECT_TEAMS_LOADED, object : IOnProjectTeamsLoadedListener {
            override fun onChange(teams: List<ProjectTeamData>)
            {
                onProjectTeamsLoaded(teams)
            }
        })
        Events.subscribe(project, "RepoInfoTab", Events.ON_PULL_REQUESTS_LOADED, object : IOnPullRequestsLoadedListener {
            override fun onChange(pullRequests: List<PullRequestData>)
            {
                onPullRequestsLoaded(pullRequests)
            }
        })
    }

    private fun onProjectTeamsLoaded(result: List<ProjectTeamData>?)
    {
        if(result == null)
        {
            loadProjectTeamsDataFailed(null)
            return
        }

        val newContent = panel {
            result.forEach {
                twoColumnsRow(
                    {text(HtmlUtils.boldText(it.name))},
                    {comment(it.description)}
                )
            }
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(projectTeamsContentPanel, newContent)
        }
    }

    private fun onPullRequestsLoaded(pullRequests: List<PullRequestData>)
    {
        val activePullRequests = pullRequests.filter { it.status == PullRequestData.STATUS_ACTIVE }
        val completedPullRequests = pullRequests.filter { it.status == PullRequestData.STATUS_COMPLETED }
        val notActivePullRequests = pullRequests.filter { it.status != PullRequestData.STATUS_ACTIVE && it.status != PullRequestData.STATUS_COMPLETED }

        val newContent = panel {
            if(activePullRequests.isEmpty())
            {
                row {
                    button("Create Pull Request") {
                        CreatePullRequestWindow(project, cachedData).show()
                    }
                }
            }

            activePullRequests.forEach { row { cell(getPullRequestGroup(it)) } }

            if(notActivePullRequests.isNotEmpty())
            {
                val groupTitle = HtmlUtils.getHtml("<b style='color: orange;'>Not active pull requests</b>", listOf())
                collapsibleGroup(groupTitle) {
                    notActivePullRequests.forEach { row { cell(getPullRequestGroup(it)) } }
                }
            }

            if(completedPullRequests.isNotEmpty())
            {
                val groupTitle = HtmlUtils.getHtml("<b style='color: green;'>Completed pull requests</b>", listOf())
                collapsibleGroup(groupTitle) {
                    completedPullRequests.forEach { row { cell(getPullRequestGroup(it)) } }
                }
            }
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(pullRequestContentPanel, newContent)
        }
    }

    private fun getPullRequestGroup(pullRequest: PullRequestData): JPanel
    {
        val groupTitle = HtmlUtils.getHtml("<b style='color: #8c9496;'>${pullRequest.pullRequestId}</b> <b>${pullRequest.title}</b>", listOf())

        return panel {
            collapsibleGroup(groupTitle) {
                twoColumnsRow(
                    {
                        link("Open in browser") { _ ->
                            BrowserUtil.browse("${cachedData.repositoryData!!.webUrl}/pullRequest/${pullRequest.pullRequestId}")
                        }
                    },
                    {
                        button("Open Pull Request") { _ ->
                            cachedData.lastOpenedPullRequest = LastOpenedPullRequestData(pullRequest)
                            PullRequestToolWindowFactory.createToolWindow(project, connection)
                        }
                    }
                )
                twoColumnsRow(
                    { label("Created by:") },
                    {
                        runCatching {
                            cell(
                                PanelBuilder { row { column { asyncIcon(pullRequest.createdBy.getImageIconAsync()) } }}.build()
                            ).gap(RightGap.SMALL)
                        }

                        text("<b>${pullRequest.createdBy.displayName}</b>")
                    }
                )
                twoColumnsRow(
                    { label("Status:") },
                    {
                        val isDraftMessage = if(pullRequest.isDraft) " (Draft)" else ""
                        text("<b>${pullRequest.status}</b>" + isDraftMessage)
                    }
                )
                twoColumnsRow(
                    { label("Source branch:") },
                    { text("<b>${StringUtils.cleanRepositoryRefName(pullRequest.sourceRefName)}</b>") }
                )
                twoColumnsRow(
                    { label("Target branch:") },
                    { text("<b>${StringUtils.cleanRepositoryRefName(pullRequest.targetRefName)}</b>") }
                )
                twoColumnsRow(
                    { label("Reviewers:") },
                    { cell(getReviewersPanel(pullRequest)) }
                )
            }
        }
    }

    private fun loadProjectTeamsDataFailed(exception: Throwable?)
    {
        NotificationUtils.notify(
            "Failed to fetch project teams from Azure DevOps",
            exception?.message ?: "Unexpected error happened while fetching project teams from Azure REST API",
            project = project,
            isAllowed = settings.notificationStates.toolWindowInitialization
        )
        PullRequestToolWindowFactory.removeIfExists(project)
    }

    override fun getName(): String = "Info"

    override fun getDescription(): String = "Basic information about the current connection and repository."

    override fun getColor(): Color? = null
}