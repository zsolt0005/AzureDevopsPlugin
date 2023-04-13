package sportisimo.ui.tabs.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import icons.CollaborationToolsIcons
import icons.CustomIcons
import sportisimo.azure.Connection
import sportisimo.data.ActionButtonData
import sportisimo.data.azure.*
import sportisimo.events.Events
import sportisimo.events.listeners.IOnPullRequestPipelineRunLoadedListener
import sportisimo.events.listeners.IOnPullRequestWorkItemsLoadedListener
import sportisimo.events.listeners.IOnPullRequestsLoadedListener
import sportisimo.events.listeners.IOnUpdatePullRequestFailedListener
import sportisimo.renderers.combobox.ActionButtonComboBoxRenderer
import sportisimo.services.DataProviderService
import sportisimo.services.PullRequestService
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.PullRequestToolWindowFactory
import sportisimo.ui.SelectAutoCompleteOptionsWindow
import sportisimo.ui.builders.PanelBuilder
import sportisimo.ui.elements.ListPanel
import sportisimo.ui.elements.adapters.WorkItemAdapter
import sportisimo.utils.*
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu
import javax.swing.SwingUtilities

class PullRequestInfoTab(
    private val project: Project,
    private val connection: Connection
): ATab()
{
    private val service = project.service<DataProviderService>()
    private val cachedData = ProjectDataState.getInstance(project)

    private lateinit var titlePanel: JPanel
    private lateinit var actionsPanel: JPanel
    private lateinit var pipelinePanel: JPanel
    private lateinit var reviewersPanel: JPanel
    private lateinit var workItemsPanel: JPanel
    private lateinit var overviewPanel: JPanel

    override fun getPanel(): JPanel
    {
        // The base panel has to be with a border layout, so it works with both dsl builder and our custom builder
        titlePanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        actionsPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        pipelinePanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        reviewersPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        workItemsPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())
        overviewPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())

        val panel = PanelBuilder {
            indent {
                row { column { add(titlePanel) } }
                group("Actions", true) { row { column { add(actionsPanel) } } }
                group("Pipeline", true) { row { column { add(pipelinePanel) } } }
                group("Reviewers", true) { row { column { add(reviewersPanel) } } }
                group("Work Items", true) { row { column { add(workItemsPanel) } } }
                group("Overview", true) { row { column { add(overviewPanel) } } }
            }
        }.build()

        registerEvents()

        service.getPullRequestsAsync()

        return UIUtils.createScrollablePanel(panel)
    }

    private fun registerEvents()
    {
        Events.subscribe(project, "PullRequestInfoTab", Events.ON_PULL_REQUEST_WORK_ITEMS_LOADED, object: IOnPullRequestWorkItemsLoadedListener {
            override fun onChange(workItems: List<WorkItemData>)
            {
                onPullRequestWorkItemsLoaded(workItems)
            }
        })
        Events.subscribe(project, "PullRequestInfoTab", Events.ON_PULL_REQUEST_PIPELINE_RUN_LOADED, object: IOnPullRequestPipelineRunLoadedListener {
            override fun onChange(run: BuildRunData?)
            {
                onPullRequestPipelineRunLoaded(run)
            }
        })
        Events.subscribe(project, "PullRequestInfoTab", Events.ON_UPDATE_PULL_REQUEST_FAILED, object: IOnUpdatePullRequestFailedListener {
            override fun onChange(errorMessage: String)
            {
                NotificationUtils.notify("Failed to update pull request", errorMessage, project = project)
            }
        })
        Events.subscribe(project, "PullRequestInfoTab", Events.ON_PULL_REQUESTS_LOADED, object: IOnPullRequestsLoadedListener {
            override fun onChange(pullRequests: List<PullRequestData>)
            {
                onPullRequestsLoaded()
            }
        })
    }

    private fun onPullRequestsLoaded()
    {
        val pullRequest = service.getPullRequest()
        if (pullRequest == null)
        {
            PullRequestToolWindowFactory.removeIfExists(project)
            return
        }

        showTitle(pullRequest)
        showActionsGroup(pullRequest)
        showOverview(pullRequest)
        showReviewers(pullRequest)

        service.getPullRequestWorkItemsAsync()
        service.getPipelineRunAsync()
    }

    private fun onPullRequestWorkItemsLoaded(workItems: List<WorkItemData>)
    {
        var workItemsList: ListPanel<WorkItemData>? = null
        workItemsList = ListPanel(
            workItems,
            4,
            WorkItemAdapter(
                {
                    it ?: return@WorkItemAdapter
                    if(it.button != MouseEvent.BUTTON3) return@WorkItemAdapter

                    val panel = it.source as JPanel
                    val selectedItem = workItemsList!!.getItem(panel) ?: return@WorkItemAdapter

                    val contextMenu = JPopupMenu()

                    val openInBrowserAction = JMenuItem("Open in browser", AllIcons.General.Web).apply {
                        addActionListener { BrowserUtil.browse(selectedItem.links.html.href) }
                    }

                    /*val removeAction = JMenuItem("Remove", AllIcons.Actions.GC).apply {
                        addActionListener {
                            val confirmation = ConfirmDialogUtils.yesNo(
                                "Delete Work Item",
                                "Are you sure you want to delete this work item ${selectedItem.id}?"
                            )

                            if (confirmation != JOptionPane.YES_OPTION)
                            {
                                return@addActionListener
                            }

                            PullRequestService.removeWorkItem(connection, toolWindowState, pullRequestId, selectedItem)
                        }
                    }*/

                    contextMenu.add(openInBrowserAction)
                    //contextMenu.add(removeAction)

                    contextMenu.show(it.component, it.x, it.y)
                }
            )
        )

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(workItemsPanel, workItemsList)
        }
    }

    private fun onPullRequestPipelineRunLoaded(pipelineRun: BuildRunData?)
    {
        if(pipelineRun == null)
        {
            val newContent = PanelBuilder { row { column { comment("Not run yet") }  } }.build()
            ThreadingManager.executeOnDispatchThreadAndAwaitResult {
                UIUtils.applyPanelToComponent(pipelinePanel, newContent)
            }
            return
        }

        showPipelineInfo(pipelineRun)
    }
    
    private fun showTitle(pullRequest: PullRequestData)
    {
        val cleanedSourceBranchName = StringUtils.cleanRepositoryRefName(pullRequest.sourceRefName)
        val cleanedTargetBranchName = StringUtils.cleanRepositoryRefName(pullRequest.targetRefName)

        val newContent = panel {
            row { text(HtmlUtils.boldText(pullRequest.title, 1.5f)) }
            row { text("<b style='color: #8c9496;'>$cleanedSourceBranchName</b> into <b style='color: #8c9496;'>$cleanedTargetBranchName</b>") }
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(titlePanel, newContent)
        }
    }

    private fun showActionsGroup(pullRequestData: PullRequestData)
    {
        val newContent = panel {
            row {
                val pullRequestActions = comboBox(
                    preparePullRequestActionsButtonsData(pullRequestData),
                    ActionButtonComboBoxRenderer()
                )
                pullRequestActions.component.addActionListener { onComboBoxChanged(pullRequestActions.component) }

                val pullRequestVotes = comboBox(
                    preparePullRequestVotesButtonsData(pullRequestData),
                    ActionButtonComboBoxRenderer()
                )
                pullRequestVotes.component.addActionListener { onComboBoxChanged(pullRequestVotes.component) }
            }
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(actionsPanel, newContent)
        }
    }

    private fun showPipelineInfo(pipelineRun: BuildRunData)
    {
        val newContent = PanelBuilder {
            row {
                column {
                    icon(pipelineRun.getResultIcon()); horizontalGap(12)
                    link("Open in browser") { BrowserUtil.browse(pipelineRun.links.web.href) }
                }
            }
            row {
                column {
                    val pipelineTime = pipelineRun.finishTime ?: pipelineRun.startTime ?: pipelineRun.queueTime
                    runCatching {
                        comment(DateTimeUtils.format(pipelineTime))
                    }
                        .onFailure {
                            comment(pipelineTime)
                        }
                }
            }
        }.build()

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(pipelinePanel, newContent)
        }
    }

    private fun showOverview(pullRequestData: PullRequestData)
    {
        val newContent = panel {
            twoColumnsRow(
                {
                    link("Open in browser") { _ ->
                        BrowserUtil.browse("${cachedData.repositoryData!!.webUrl}/pullRequest/${pullRequestData.pullRequestId}")
                    }
                },
                {
                    text("<b style='color: #8c9496;'>${pullRequestData.pullRequestId}</b> <b>${pullRequestData.title}</b>")
                }
            )
            twoColumnsRow(
                { label("Created by:") },
                {
                    runCatching {
                        cell(
                            PanelBuilder { row { column { asyncIcon(pullRequestData.createdBy.getImageIconAsync()) } }}.build()
                        ).gap(RightGap.SMALL)
                    }

                    text("<b>${pullRequestData.createdBy.displayName}</b>")
                }
            )
            twoColumnsRow(
                { label("Status:") },
                {
                    val isDraftMessage = if(pullRequestData.isDraft) " (Draft)" else ""
                    text("<b>${pullRequestData.status}</b>" + isDraftMessage)
                }
            )
            twoColumnsRow(
                { label("Source branch:") },
                { text("<b>${StringUtils.cleanRepositoryRefName(pullRequestData.sourceRefName)}</b>") }
            )
            twoColumnsRow(
                { label("Target branch:") },
                { text("<b>${StringUtils.cleanRepositoryRefName(pullRequestData.targetRefName)}</b>") }
            )
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(overviewPanel, newContent)
        }
    }

    private fun showReviewers(pullRequestData: PullRequestData)
    {
        val newContent = getReviewersPanel(pullRequestData)

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(reviewersPanel, newContent)
        }
    }

    private fun onComboBoxChanged(comboBox: ComboBox<ActionButtonData>)
    {
        val actionButtonData = comboBox.selectedItem as ActionButtonData
        comboBox.isEnabled = false
        comboBox.selectedIndex = 0

        ThreadingManager.executeOnPooledThread {
            actionButtonData.callback.call()

            ThreadingManager.executeOnDispatchThreadAndAwaitResult { comboBox.isEnabled = true }
        }
    }

    private fun preparePullRequestVotesButtonsData(pullRequest: PullRequestData): List<ActionButtonData>
    {
        val actionButtonsData = mutableListOf<ActionButtonData>()

        if(pullRequest.status == PullRequestData.STATUS_ABANDONED)
        {
            actionButtonsData.add(ActionButtonData("Reactivate", AllIcons.Actions.Refresh ) { PullRequestService.reactivate(connection, cachedData, pullRequest, project) })
            return actionButtonsData
        }

        if(pullRequest.isDraft)
        {
            actionButtonsData.add(ActionButtonData("Publish", CollaborationToolsIcons.Send ) { PullRequestService.publish(connection, cachedData, pullRequest, project) })
        }
        else
        {
            if(pullRequest.autoCompleteSetBy == null)
            {
                actionButtonsData.add(ActionButtonData("Set Auto Complete", AllIcons.Actions.Lightning) {
                    SwingUtilities.invokeLater {
                        val completionOptions = PullRequestCompletionOptions()

                        val window = SelectAutoCompleteOptionsWindow(
                            StringUtils.cleanRepositoryRefName(pullRequest.sourceRefName),
                            completionOptions
                        )
                        window.show()

                        if(!window.isOK) return@invokeLater

                        ThreadingManager.executeOnPooledThread {
                            PullRequestService.setAutoComplete(connection, cachedData, pullRequest, completionOptions, project)
                        }
                    }
                })
            }
            else
            {
                actionButtonsData.add(ActionButtonData("Cancel Auto Complete", AllIcons.Actions.Cancel) { PullRequestService.cancelAutoComplete(connection, cachedData, pullRequest, project) })
            }

            actionButtonsData.add(ActionButtonData("Complete", AllIcons.Vcs.Merge) { PullRequestService.complete(connection, cachedData, pullRequest, project) })
            actionButtonsData.add(ActionButtonData("Mark As Draft", AllIcons.Actions.Edit) { PullRequestService.markAsDraft(connection, cachedData, pullRequest, project) })
        }

        actionButtonsData.add(ActionButtonData("Abandon", AllIcons.Actions.GC) { PullRequestService.abandon(connection, cachedData, pullRequest, project) })

        return actionButtonsData
    }

    private fun preparePullRequestActionsButtonsData(pullRequest: PullRequestData): List<ActionButtonData>
    {
        return listOf(
            ActionButtonData("Approve", CustomIcons.PullRequestApproved) { PullRequestService.updateVote(ReviewerData.VOTE_APPROVED, connection, cachedData, pullRequest, project) },
            ActionButtonData("Approve With Suggestions", CustomIcons.PullRequestApproved) { PullRequestService.updateVote(ReviewerData.VOTE_APPROVED_WITH_SUGGESTIONS, connection, cachedData, pullRequest, project)  },
            ActionButtonData("Waiting For Author", CustomIcons.PullRequestWaitingForAuthor) { PullRequestService.updateVote(ReviewerData.VOTE_WAITING_FOR_AUTHOR, connection, cachedData, pullRequest, project) },
            ActionButtonData("Reject", CustomIcons.PullRequestRejected) { PullRequestService.updateVote(ReviewerData.VOTE_REJECT, connection, cachedData, pullRequest, project) },
            ActionButtonData("Reset Feedback", CustomIcons.PullRequestNotVoted) { PullRequestService.updateVote(ReviewerData.VOTE_NOT_VOTED, connection, cachedData, pullRequest, project) }
        )
    }

    override fun getName(): String = "Overview"
    override fun getDescription(): String =  "Overview of the Pull request"
    override fun getColor(): Color? = null
}