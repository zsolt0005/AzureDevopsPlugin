package sportisimo.ui

import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.util.ui.JBUI
import sportisimo.azure.Connection
import sportisimo.data.azure.RepositoryRefData
import sportisimo.data.azure.SubjectQueryResultData
import sportisimo.data.azure.WorkItemSearchResultData
import sportisimo.data.azure.client.*
import sportisimo.data.builders.ColumnAlignment
import sportisimo.data.ui.ActionData
import sportisimo.events.Events
import sportisimo.services.DataProviderService
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.builders.PanelBuilder
import sportisimo.ui.elements.ListPanel
import sportisimo.ui.elements.adapters.SubjectQueryResultAdapter
import sportisimo.ui.elements.adapters.WorkItemSearchResultAdapter
import sportisimo.utils.EventUtils
import sportisimo.utils.ListHelper
import sportisimo.utils.NotificationUtils
import sportisimo.utils.StringUtils
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import javax.swing.*

class CreatePullRequestWindow(
    private val project: Project,
    private val saveData: ProjectDataState
): DialogWrapper(true)
{
    private val service = project.service<DataProviderService>()
    private val connection = Connection(saveData.connectionData!!)

    init
    {
        title = "Create Pull Request"
        init()
    }

    private lateinit var loadingAnimation: JBLabel
    private lateinit var errorMessageLabel: JBLabel
    private lateinit var createPullRequestAction: Action
    private lateinit var createDraftAction: Action

    private lateinit var branchComboBox: ComboBox<RepositoryRefData>
    private lateinit var titleTextField: JTextField
    private lateinit var descriptionTextArea: JTextArea

    private lateinit var workItemSearchTextField: JTextField
    private lateinit var foundWorkItemsPanel: ListPanel<WorkItemSearchResultData>
    private lateinit var selectedWorkItemsPanel: ListPanel<WorkItemSearchResultData>

    private lateinit var reviewerSearchTextField: JTextField
    private lateinit var foundReviewersPanel: ListPanel<SubjectQueryResultData>
    private lateinit var selectedReviewersPanel: ListPanel<SubjectQueryResultData>

    private var foundWorkItems: List<WorkItemSearchResultData> = listOf()
    private var foundUsers: List<SubjectQueryResultData> = listOf()

    override fun createCenterPanel(): JComponent
    {
        return PanelBuilder {
            preferredSize(500, 0)
            border(JBUI.Borders.empty(8))

            row {
                column {
                    val branchName = StringUtils.cleanRepositoryRefName(saveData.branchData!!.name)
                    label("Source branch: ")
                    boldLabel(branchName)
                }
            }
            emptyRow()

            row { column { boldLabel("Target branch:"); boldLabel("*").apply { foreground = JBColor.RED } } }
            row { column { branchComboBox = comboBox(listOf(), "No branches found") } }
            emptyRow()

            row { column { boldLabel("Title:"); boldLabel("*").apply { foreground = JBColor.RED } } }
            row { column { titleTextField = textField() } }
            emptyRow()

            row { column { boldLabel("Description:") } }
            row { column { descriptionTextArea = textArea(4) } }
            emptyRow()

            emptyRow()
            row { column { boldLabel("Search Work Items:") } }
            row { column { workItemSearchTextField = searchTextField() } }
            row { column {
                foundWorkItemsPanel = listPanel(
                    listOf(),
                    2,
                    WorkItemSearchResultAdapter({
                        if(it?.button != MouseEvent.BUTTON1) return@WorkItemSearchResultAdapter

                        val panel = it.source as JPanel
                        val selectedItem = foundWorkItemsPanel.getItem(panel) ?: return@WorkItemSearchResultAdapter

                        selectedWorkItemsPanel.addItem(selectedItem)

                        showFoundWorkItems()
                    })
                )
            } }
            emptyRow()

            separator("Selected Work Items")
            row { column {
                selectedWorkItemsPanel = listPanel(
                    listOf(),
                    adapter = WorkItemSearchResultAdapter(
                        {
                            it ?: return@WorkItemSearchResultAdapter
                            if(it.button != MouseEvent.BUTTON3) return@WorkItemSearchResultAdapter

                            val panel = it.source as JPanel
                            val selectedItem = selectedWorkItemsPanel.getItem(panel) ?: return@WorkItemSearchResultAdapter
                            val title = "${selectedItem.fields.workItemType} ${selectedItem.fields.id}: ${selectedItem.fields.title}"

                            val contextMenu = JPopupMenu()
                            val setAsTitleAction = JMenuItem("Set as title").apply {
                                addActionListener { titleTextField.text = title  }
                            }

                            val addToDescriptionAction = JMenuItem("Append to description").apply {
                                addActionListener { descriptionTextArea.append(title) }
                            }

                            val removeAction = JMenuItem("Remove", AllIcons.Actions.GC).apply {
                                addActionListener {
                                    selectedWorkItemsPanel.removeItem(selectedItem)

                                    showFoundWorkItems()
                                }
                            }

                            contextMenu.add(setAsTitleAction)
                            contextMenu.add(addToDescriptionAction)
                            contextMenu.add(removeAction)

                            contextMenu.show(it.component, it.x, it.y)
                        },
                        action = ActionData(AllIcons.Actions.GC) { e, panel ->
                            if(e?.button != MouseEvent.BUTTON1) return@ActionData

                            val selectedItem = selectedWorkItemsPanel.getItem(panel) ?: return@ActionData
                            selectedWorkItemsPanel.removeItem(selectedItem)

                            showFoundWorkItems()
                        }
                    )
                )
            } }
            emptyRow()
            emptyRow()


            row { column { boldLabel("Search Reviewers:") } }
            row { column { reviewerSearchTextField = searchTextField() } }
            row { column {
                foundReviewersPanel = listPanel(
                    listOf(),
                    2,
                    SubjectQueryResultAdapter(false, {
                        if(it?.button != MouseEvent.BUTTON1) return@SubjectQueryResultAdapter

                        val panel = it.source as JPanel
                        val selectedItem = foundReviewersPanel.getItem(panel) ?: return@SubjectQueryResultAdapter

                        selectedReviewersPanel.addItem(selectedItem)

                        showFoundReviewers()
                    })
                )
            } }
            emptyRow()

            separator("Selected Reviewers")
            row { column {
                selectedReviewersPanel = listPanel(
                    listOf(),
                    adapter = SubjectQueryResultAdapter(true,
                        action = ActionData(AllIcons.Actions.GC) { e, panel ->
                            if(e?.button != MouseEvent.BUTTON1) return@ActionData

                            val selectedItem = selectedReviewersPanel.getItem(panel) ?: return@ActionData
                            selectedReviewersPanel.removeItem(selectedItem)

                            showFoundReviewers()
                        }
                    )
                )
            } }
            emptyRow()

            row {
                column(ColumnAlignment.Center) {
                    errorMessageLabel = boldLabel("").apply {
                        foreground = JBColor.RED
                    }
                }
            }
            row {
                column(ColumnAlignment.Center) {
                    loadingAnimation = loading().apply {
                        isVisible = false
                    }
                }
            }
            emptyRow()

            showBranches()

            initListeners()
        }
        .build()
    }

    private fun createPullRequest(isDraft: Boolean = false)
    {
        if(titleTextField.text.isEmpty())
        {
            errorMessageLabel.text = "Title is required!"
            return
        }

        val targetBranchData = branchComboBox.selectedItem as RepositoryRefData
        val reviewersData = mutableListOf<NewPullRequestReviewerData>()
        val workItemsData = mutableListOf<NewPullRequestResourceRef>()

        selectedReviewersPanel.getItems().forEach {
            val storageKey = connection.graphClient.getStorageKeyByDescriptor(it.links!!.storageKey.href) ?: return

            reviewersData.add(
                NewPullRequestReviewerData(
                    storageKey,
                    it.isRequired
                )
            )
        }

        selectedWorkItemsPanel.getItems().forEach {
            workItemsData.add(
                NewPullRequestResourceRef(
                    it.fields.id,
                    it.url
                )
            )
        }

        val pullRequestData = NewPullRequestData(
            sourceRefName = saveData.branchData!!.name,
            targetRefName = targetBranchData.name,
            title = titleTextField.text,
            description = descriptionTextArea.text,
            isDraft = isDraft,
            reviewers = reviewersData,
            workItemRefs = workItemsData
        )

        loadingAnimation.isVisible = true
        createPullRequestAction.isEnabled = false
        createDraftAction.isEnabled = false

        ThreadingManager.executeOnPooledThread {
            runCatching {
                connection.gitClient.createPullRequest(
                    saveData.connectionData!!.project,
                    saveData.repositoryData!!,
                    pullRequestData
                )
            }
                .onFailure {
                    loadingAnimation.isVisible = false
                    createPullRequestAction.isEnabled = true
                    createDraftAction.isEnabled = true
                    errorMessageLabel.text = it.message
                }
                .onSuccess {
                    BackgroundTaskUtil.syncPublisher(project, Events.ON_UPDATE_DEVOPS_TOOL_WINDOW).onChange()

                    SwingUtilities.invokeLater {
                        close(OK_EXIT_CODE)
                    }
                }
        }
    }

    private fun tryCreatePullRequest(isDraft: Boolean = false)
    {
        runCatching {
            createPullRequest(isDraft)
        }
            .onFailure {
                NotificationUtils.notify(
                    "Failed to create a new pull-request",
                    it.message ?: "",
                    NotificationType.ERROR,
                    asPopup = true
                )
            }
    }

    override fun createActions(): Array<Action>
    {
        createPullRequestAction = getPrimaryAction { tryCreatePullRequest(false) }
        createDraftAction = getSecondaryAction { tryCreatePullRequest(true) }

        return arrayOf(
            createPullRequestAction,
            createDraftAction
        )
    }

    private fun showBranches()
    {
        runCatching {
            val branches = connection.gitClient.getBranches(saveData.repositoryData!!)
                .filter { it.name.contains("refs/heads") }
                .filter { it.name != saveData.branchData!!.name }
                .sortedWith(ListHelper.getRepositorySorter())

            branchComboBox.removeAllItems()
            branches.forEach { branchComboBox.addItem(it) }
        }
    }

    private fun initListeners()
    {
        workItemSearchTextField.document.addDocumentListener(EventUtils.DocumentEvents.onAllDocumentEvent { searchWorkItems() })
        reviewerSearchTextField.document.addDocumentListener(EventUtils.DocumentEvents.onAllDocumentEvent { searchReviewers() })
    }

    private fun searchWorkItems()
    {
        val searchData = WorkItemSearchData(
            workItemSearchTextField.text,
            top = 10
        )

        service.getWorkItemsAsync(
            searchData,
            onFailed = { foundWorkItems = listOf(); showFoundWorkItems() },
            onLoaded = { foundWorkItems = it; showFoundWorkItems() }
        )
    }

    private fun searchReviewers()
    {
        val searchData = SubjectQuerySearchData(
            listOf(SubjectQuerySearchSubjectKind.User.value),
            reviewerSearchTextField.text
        )

        service.getUsersAsync(
            searchData,
            onFailed = { foundUsers = listOf(); showFoundReviewers()},
            onLoaded = { foundUsers = it; showFoundReviewers()}
        )
    }

    private fun showFoundWorkItems()
    {
        SwingUtilities.invokeLater {
            foundWorkItemsPanel.removeAllItems()

            val alreadyAdded = selectedWorkItemsPanel.getItems()

            foundWorkItems.forEach {
                val contains = alreadyAdded.any { alreadyAddedItem -> alreadyAddedItem.fields.id == it.fields.id }
                if(contains) return@forEach

                foundWorkItemsPanel.addItem(it)
            }
        }
    }

    private fun showFoundReviewers()
    {
        SwingUtilities.invokeLater {
            foundReviewersPanel.removeAllItems()

            val alreadyAdded = selectedReviewersPanel.getItems()

            foundUsers.forEach {
                val contains = alreadyAdded.any { alreadyAddedItem -> alreadyAddedItem.descriptor == it.descriptor }
                if(contains) return@forEach

                foundReviewersPanel.addItem(it)
            }
        }
    }

    /**
     * There can be only one default action for a dialog. If multiple added, the last added will be the primary
     */
    private fun getPrimaryAction(callback: (ActionEvent?) -> Unit): Action
    {
        return getAction("Create", callback, Pair(DEFAULT_ACTION, true))
    }

    private fun getSecondaryAction(callback: (ActionEvent?) -> Unit): Action
    {
        return getAction("Create as draft", callback, Pair(FOCUSED_ACTION, true))
    }

    private fun getAction(text: String, callback: (ActionEvent?) -> Unit, putValue: Pair<String, Any>): Action
    {
        return object: DialogWrapperAction(text)
        {
            init { putValue(putValue.first, putValue.second) }

            override fun doAction(e: ActionEvent?) { callback(e) }
        }
    }
}
