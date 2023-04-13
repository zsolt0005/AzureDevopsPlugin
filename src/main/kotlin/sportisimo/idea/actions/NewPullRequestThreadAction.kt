package sportisimo.idea.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.ThreadContextData
import sportisimo.data.azure.ThreadContextPositionData
import sportisimo.data.azure.client.NewThreadData
import sportisimo.services.DataProviderService
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.NewThreadWindow
import sportisimo.utils.FileUtils

class NewPullRequestThreadAction: AnAction()
{
    override fun update(e: AnActionEvent)
    {
        e.presentation.isEnabled = false
        val project = e.project ?: return

        val cachedData = ProjectDataState.getInstance(project)
        if(cachedData.connectionData == null) return

        DumbService.getInstance(project).runWhenSmart {
            val service = project.service<DataProviderService>()
            val pullRequest = service.getPullRequest()
            if(pullRequest != null)
            {
                e.presentation.isEnabled = true
            }
        }
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun actionPerformed(e: AnActionEvent)
    {
        val project = e.project ?: return
        val cachedData = ProjectDataState.getInstance(project)
        if(cachedData.connectionData == null) return

        DumbService.getInstance(project).runWhenSmart {
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return@runWhenSmart
            val service = project.service<DataProviderService>()

            val pullRequest = service.getPullRequest() ?: return@runWhenSmart
            openNewThreadWindow(project, editor, pullRequest)
        }
    }

    private fun openNewThreadWindow(project: Project, editor: Editor, pullRequest: PullRequestData)
    {
        val service = project.service<DataProviderService>()

        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        val filePath = FileUtils.getFilePathFromFile(file, project)
        val isLeft = FileUtils.isLeftSideFile(file)

        val startOffset = editor.selectionModel.selectionStart
        val endOffset = editor.selectionModel.selectionEnd

        val startLine = editor.offsetToLogicalPosition(startOffset)
        val endLine = editor.offsetToLogicalPosition(endOffset)

        val realStartLine = startLine.line + 1
        val realEndLine = endLine.line + 1

        var realStartOffset = startLine.column + 1
        var realEndOffset = endLine.column + 1

        if(startOffset == endOffset)
        {
            val document = editor.document

            realStartOffset = (document.getLineStartOffset(startLine.line) + 1) - startOffset
            realEndOffset = (document.getLineEndOffset(endLine.line) + 1) - endOffset
        }

        val threadContextPositionStart = ThreadContextPositionData(realStartLine, realStartOffset)
        val threadContextPositionEnd = ThreadContextPositionData(realEndLine, realEndOffset)

        val threadContext =
            if(isLeft) ThreadContextData(filePath, leftFileStart = threadContextPositionStart, leftFileEnd = threadContextPositionEnd)
            else ThreadContextData(filePath, rightFileStart = threadContextPositionStart, rightFileEnd = threadContextPositionEnd)

        val newThreadData = NewThreadData(threadContext)

        service.getCurrentUserAsync {
            ThreadingManager.executeOnDispatchThread {
                NewThreadWindow(editor.project!!, pullRequest, newThreadData, it).show()
            }
        }
    }
}