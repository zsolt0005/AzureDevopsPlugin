package sportisimo.idea

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import sportisimo.data.FileRangeData
import sportisimo.data.azure.PullRequestThreadData
import sportisimo.data.ui.AvatarIconOptionsData
import sportisimo.events.Events
import sportisimo.events.listeners.IOnPullRequestThreadsLoadedListener
import sportisimo.exceptions.PullRequestException
import sportisimo.services.DataProviderService
import sportisimo.states.AppSettingsState
import sportisimo.ui.ThreadWindow
import sportisimo.utils.FileUtils
import java.awt.Color
import javax.swing.Icon

object EditorCommentsManager
{
    private val settings = AppSettingsState.getInstance()
    private val editorHighlightersMap = mutableMapOf<Editor, MutableList<RangeHighlighter>>()

    fun opened(event: EditorFactoryEvent)
    {
        val project = event.editor.project ?: return
        val file = FileUtils.getFileByEditor(event.editor) ?: return
        val service = project.service<DataProviderService>()

        val path = FileUtils.getFilePathFromFile(file, project)

        Events.subscribe(event.editor.project!!, event.editor, Events.ON_PULL_REQUEST_COMMENTS_LOADED, object: IOnPullRequestThreadsLoadedListener
        {
            override fun onChange(threads: List<PullRequestThreadData>)
            {
                val fileComments = threads.filter { it.threadContext?.filePath == path }
                onCommentsLoaded(project, event.editor, fileComments, file)
            }
        })

        try
        {
            val lastIteration = service.getPullRequestIterations().last()
            service.getPullRequestThreadsAsync(lastIteration)
        }
        catch (e: PullRequestException)
        {
            // Ignore
        }
    }

    fun closed(event: EditorFactoryEvent)
    {
        if(event.editor.project == null) return

        Events.unSubscribe(event.editor.project!!, event.editor, Events.ON_PULL_REQUEST_COMMENTS_LOADED)
    }

    private fun onCommentsLoaded(
        project: Project,
        editor: Editor,
        threads: List<PullRequestThreadData>,
        file: VirtualFile
    )
    {
        val service = project.service<DataProviderService>()
        val pullRequest = service.getPullRequest() ?: return

        val isLeft = FileUtils.isLeftSideFile(file)

        ApplicationManager.getApplication().invokeLater {
            editorHighlightersMap[editor]?.forEach {
                editor.markupModel.removeHighlighter(it)
            }

            editorHighlightersMap[editor] = mutableListOf()

            threads.forEach { thread ->
                val range = if(isLeft)
                {
                    FileRangeData(
                        thread.threadContext?.leftFileStart?.line ?: return@forEach,
                        thread.threadContext.leftFileStart.offset,
                        thread.threadContext.leftFileEnd?.line ?: return@forEach,
                        thread.threadContext.leftFileEnd.offset,
                    )
                }
                else
                {
                    FileRangeData(
                        thread.threadContext?.rightFileStart?.line ?: return@forEach,
                        thread.threadContext.rightFileStart.offset,
                        thread.threadContext.rightFileEnd?.line ?: return@forEach,
                        thread.threadContext.rightFileEnd.offset,
                    )
                }

                if(!thread.isActive() && settings.pullRequestState.hideNotActiveComments) return@forEach

                val firstComment = thread.comments.first()

                val startOffset = editor.document.getLineStartOffset(range.startLine - 1) + range.startOffset - 1
                val endOffset = editor.document.getLineStartOffset(range.endLine - 1) + range.endOffset - 1

                val attributes =
                    if(thread.isActive())
                    {
                        TextAttributes().apply {
                            setAttributes(null, Color(0, 0, 0, 80), null, null, EffectType.BOXED, EditorFontType.PLAIN.ordinal)
                        }
                    }
                    else
                    {
                        null
                    }

                val rangeHighlighter = editor.markupModel.addRangeHighlighter(
                    startOffset,
                    endOffset,
                    HighlighterLayer.CARET_ROW,
                    attributes,
                    HighlighterTargetArea.EXACT_RANGE
                )

                rangeHighlighter.gutterIconRenderer = object: GutterIconRenderer()
                {
                    /* Not needed ? */
                    override fun equals(other: Any?): Boolean = true
                    override fun hashCode(): Int = 0

                    /* UI */
                    override fun getIcon(): Icon
                    {
                        val isGrayScale = !thread.isActive()

                        return firstComment.author.getImageIcon(AvatarIconOptionsData(16, isGrayScale))
                    }
                    override fun getAlignment(): Alignment = Alignment.CENTER
                    override fun getTooltipText(): String = firstComment.author.displayName

                    /* Action */
                    override fun getClickAction(): AnAction = object: AnAction() {
                        override fun actionPerformed(e: AnActionEvent)
                        {
                            ThreadWindow(editor.project!!, pullRequest, thread).show()
                        }
                    }
                }

                editorHighlightersMap[editor]!!.add(rangeHighlighter)
            }
        }
    }
}