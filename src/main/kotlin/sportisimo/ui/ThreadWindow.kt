package sportisimo.ui

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import sportisimo.azure.Connection
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.PullRequestThreadData
import sportisimo.events.Events
import sportisimo.events.listeners.IOnPullRequestThreadsLoadedListener
import sportisimo.services.DataProviderService
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.FileUtils
import sportisimo.utils.UIUtils
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JPanel

class ThreadWindow(
    private val project: Project,
    private val pullRequest: PullRequestData,
    private val thread: PullRequestThreadData
): DialogWrapper(project, true, IdeModalityType.MODELESS)
{
    private val service = project.service<DataProviderService>()
    private val cacheData = ProjectDataState.getInstance(project)

    init
    {
        title = FileUtils.getFileNameFromPath(thread.threadContext!!.filePath)
        init()
    }

    private lateinit var commentsPanel: JPanel

    override fun createCenterPanel(): JComponent
    {
        commentsPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())

        val panel = PanelBuilder{
            indent {
                row {
                    column {
                        add(commentsPanel)
                    }
                }
            }
        }.build()

        renderComments(thread)

        registerEvents()

        return UIUtils.createScrollablePanel(panel)
    }

    private fun registerEvents()
    {
        Events.subscribe(project, this, Events.ON_PULL_REQUEST_COMMENTS_LOADED, object: IOnPullRequestThreadsLoadedListener
        {
            override fun onChange(threads: List<PullRequestThreadData>)
            {
                runCatching {
                    val fileThread = threads.first { it.id == thread.id }
                    renderComments(fileThread)
                }.onFailure {
                    ThreadingManager.executeOnDispatchThread {
                        close(CANCEL_EXIT_CODE)
                    }
                }
            }
        })
    }

    private fun renderComments(thread: PullRequestThreadData)
    {
        val connection = Connection(cacheData.connectionData!!)
        val currentUser = service.getCurrentUser()

        val newContent = PanelBuilder {
            row {
                column {
                    add(UIUtils.createThreadCommentsPanel(project, cacheData.connectionData!!.project, thread, false, currentUser, connection, pullRequest))
                }
            }
        }.build()

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(commentsPanel, newContent)
        }
    }

    override fun doCancelAction()
    {
        Events.unSubscribe(project, this, Events.ON_PULL_REQUEST_COMMENTS_LOADED)

        super.doCancelAction()
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun createActions() = arrayOf<Action>()
}