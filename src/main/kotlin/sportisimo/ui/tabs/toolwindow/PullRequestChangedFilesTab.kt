package sportisimo.ui.tabs.toolwindow

import com.intellij.diff.DiffManager
import com.intellij.diff.DiffRequestFactory
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.dsl.builder.panel
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.PullRequestIterationChangeData
import sportisimo.data.azure.PullRequestIterationData
import sportisimo.data.enums.CommitChangeType
import sportisimo.data.renderers.FileCellData
import sportisimo.events.Events
import sportisimo.events.listeners.IOnPullRequestsLoadedListener
import sportisimo.idea.vfs.LightVirtualFile
import sportisimo.services.DataProviderService
import sportisimo.threading.ThreadingManager
import sportisimo.ui.PullRequestToolWindowFactory
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.FileTreeUtils
import sportisimo.utils.NotificationUtils
import sportisimo.utils.UIUtils
import java.awt.Color
import java.awt.event.MouseEvent
import javax.swing.JMenuItem
import javax.swing.JPanel
import javax.swing.JPopupMenu

class PullRequestChangedFilesTab(private val project: Project): ATab()
{
    private val service = project.service<DataProviderService>()

    private lateinit var changedFilesPanel: JPanel

    private val excludedForDiffView = listOf(
        CommitChangeType.Add.value,
        CommitChangeType.Delete.value,
        CommitChangeType.None.value
    )

    private val excludedForSource = listOf(
        CommitChangeType.Delete.value
    )

    override fun getPanel(): JPanel
    {
        changedFilesPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())

        val panel = PanelBuilder {
            indent {
                row { column { add(changedFilesPanel) } }
            }
        }.build()

        registerEvents()

        service.getPullRequestsAsync()

        return UIUtils.createScrollablePanel(panel)
    }

    private fun registerEvents()
    {
        Events.subscribe(project, "PullRequestFilesTab", Events.ON_PULL_REQUESTS_LOADED, object: IOnPullRequestsLoadedListener {
            override fun onChange(pullRequests: List<PullRequestData>)
            {
                onPullRequestsLoaded()
            }
        })
    }

    private fun onPullRequestsLoaded() = ThreadingManager.executeOnPooledThread {
        val pullRequest = service.getPullRequest()

        if (pullRequest == null)
        {
            PullRequestToolWindowFactory.removeIfExists(project)
            return@executeOnPooledThread
        }

        service.getPullRequestIterationsAsync(true) {
            onIterationsLoaded(it)
        }
    }

    private fun onIterationsLoaded(
        iterations: List<PullRequestIterationData>
    ) = ThreadingManager.executeOnPooledThread {
        if(iterations.isEmpty())
        {
            noChangedFilesFound()
            return@executeOnPooledThread
        }

        val from = iterations.last()

        val changes = service.getPullRequestIterationsComparison(from)
        if(changes.isEmpty())
        {
            noChangedFilesFound()
            return@executeOnPooledThread
        }

        onChangedFilesLoaded(changes)
    }

    private fun noChangedFilesFound(errorMessage: String? = null)
    {
        val newContent = panel {
            row {
                if(errorMessage.isNullOrEmpty())
                {
                    comment("No changes found")
                }
                else
                {
                    text("<b style='color: red;'>${errorMessage}</b>")
                }
            }

        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(changedFilesPanel, newContent)
        }
    }

    private fun onChangedFilesLoaded(changedFilesData: List<PullRequestIterationChangeData>)
    {
        val rootNode = FileTreeUtils.prepareChangedFilesNodes(project, changedFilesData)
        val tree = FileTreeUtils.getFileTree(rootNode) { e, data ->
            if(
                data.isFolder
             || data.changedFileData!!.changeType == CommitChangeType.Delete.value
            )
            {
                return@getFileTree
            }

            if(e?.button == MouseEvent.BUTTON1 && e.clickCount == 2)
            {
                onFileDoubleClickEvent(data)
            }

            if(e?.button == MouseEvent.BUTTON3)
            {
                onFileRightClickEvent(e, data)
            }
        }

        val newContent = panel {
            row { cell(tree) }
        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(changedFilesPanel, newContent)
        }
    }

    private fun onFileDoubleClickEvent(data: FileCellData)
    {
        val showSourceFile = !excludedForSource.contains(data.changedFileData!!.changeType)
        if(!showSourceFile) return

        openSourceFile(data)
    }

    private fun onFileRightClickEvent(e: MouseEvent, data: FileCellData)
    {
        val showDiffView = !excludedForDiffView.contains(data.changedFileData!!.changeType)
        val showSourceFile = !excludedForSource.contains(data.changedFileData.changeType)

        val contextMenu = JPopupMenu()

        if(showDiffView)
        {
            val diffView = JMenuItem("Diff view", AllIcons.Actions.Diff).apply {
                addActionListener {
                    openSplitChangesView(data)
                }
            }
            contextMenu.add(diffView)
        }

        if(showSourceFile)
        {
            val sourceFile = JMenuItem("Source file", AllIcons.Actions.EditSource).apply {
                addActionListener {
                    openSourceFile(data)
                }
            }
            contextMenu.add(sourceFile)
        }

        contextMenu.show(e.component, e.x, e.y)
    }

    private fun openSourceFile(data: FileCellData)
    {
        val file = VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}/${data.path}") ?: return
        FileEditorManager.getInstance(project).openFile(file, true)
    }

    private fun openSplitChangesView(data: FileCellData) = ThreadingManager.executeOnPooledThread {
        val targetBranchLastCommit = service.getTargetBranchCommits().firstOrNull()
        if(targetBranchLastCommit == null)
        {
            NotificationUtils.notify(
                "Failed to open diff view",
                "Diff view can not be opened because detecting the last commit of the target branch returned nothing.",
                NotificationType.ERROR,
                true,
                project = project
            )
            return@executeOnPooledThread
        }

        val changedFileData = data.changedFileData!!

        val originalFilePath = changedFileData.originalPath ?: changedFileData.item.path!!
        val originalFileData = service.getGitItemByPathAndVersion(originalFilePath, targetBranchLastCommit.commitId) ?: return@executeOnPooledThread

        val currentFile = VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}/${changedFileData.item.path}") ?: return@executeOnPooledThread
        val originalFile = LightVirtualFile(
            "Original",
            originalFileData.content!!,
            changedFileData.item.path!!
        ).apply {
            fileType = currentFile.fileType
            isWritable = false
        }

        val diffRequest = DiffRequestFactory.getInstance().createFromFiles(project, originalFile, currentFile)

        ApplicationManager.getApplication().invokeLater {
            DiffManager.getInstance().showDiff(project, diffRequest)
        }
    }

    override fun getName(): String = "Changed Files"
    override fun getDescription(): String =  "Changed files"
    override fun getColor(): Color? = null
}