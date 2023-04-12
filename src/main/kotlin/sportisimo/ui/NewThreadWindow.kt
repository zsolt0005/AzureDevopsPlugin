package sportisimo.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.panel
import sportisimo.azure.Connection
import sportisimo.data.azure.IdentityData
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.client.NewPullRequestThreadCommentData
import sportisimo.data.azure.client.NewThreadData
import sportisimo.services.PullRequestThreadService
import sportisimo.states.ProjectDataState
import sportisimo.ui.builders.PanelBuilder
import sportisimo.ui.elements.EditorPanel
import sportisimo.utils.FileUtils
import sportisimo.utils.HtmlUtils
import sportisimo.utils.UIUtils
import java.awt.Dimension
import javax.swing.Action
import javax.swing.JComponent

class NewThreadWindow(
    private val project: Project,
    private val pullRequest: PullRequestData,
    private val thread: NewThreadData,
    private val currentUser: IdentityData
): DialogWrapper(project, true, IdeModalityType.MODELESS)
{
    private val cacheData = ProjectDataState.getInstance(project)

    init
    {
        title = FileUtils.getFileNameFromPath(thread.threadContext.filePath)
        init()
    }

    override fun createCenterPanel(): JComponent
    {
        val connection = Connection(cacheData.connectionData!!)

        val pullRequestThreadService = service<PullRequestThreadService>()

        val preferredWidth = 500
        setSize((preferredWidth * 1.2).toInt(), 500)
        val editorPanel = EditorPanel("Comment")
        editorPanel.showBottomPanel()

        val fileName = FileUtils.getFileNameFromPath(thread.threadContext.filePath)
        val filePath = thread.threadContext.filePath

        val title = HtmlUtils.getHtml("<b>$fileName</b> <span style='color: #8c9496;'>$filePath</span>", listOf())

        val innerPanel = PanelBuilder {
            row {
                column {
                    panel {
                        row { column { asyncIcon(currentUser.getImageIconAsync()) } }
                        rowFiller()
                    }.apply {
                        this.maximumSize = Dimension(24, Int.MAX_VALUE) // TODO Hardcoded value of the icon panel size
                    }
                }
                column {
                    add(editorPanel)
                }
            }
        }.build().apply {
            preferredSize = Dimension(preferredWidth, preferredSize.height)
        }

        val panel = panel {
            indent {
                group(title, true) {
                    row {
                        cell(innerPanel)
                    }
                }
            }
        }

        editorPanel.addOnTextAreaUpdated {
            innerPanel.preferredSize = Dimension(preferredWidth, editorPanel.preferredSize.height)
            innerPanel.revalidate()
        }

        editorPanel.addOnReplyClicked {
            thread.comments.clear()
            thread.comments.add(NewPullRequestThreadCommentData(it))

            val createdThread = pullRequestThreadService.createThread(project, connection, cacheData.connectionData!!.project, pullRequest, thread)
            ThreadWindow(project, pullRequest, createdThread).show()
            close(OK_EXIT_CODE)
        }

        return UIUtils.createScrollablePanel(panel)
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun createActions() = arrayOf<Action>()
}