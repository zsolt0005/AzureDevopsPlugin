package sportisimo.ui.tabs.toolwindow

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.panel
import sportisimo.azure.Connection
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.PullRequestThreadData
import sportisimo.events.Events
import sportisimo.events.listeners.IOnPullRequestThreadsLoadedListener
import sportisimo.events.listeners.IOnPullRequestsLoadedListener
import sportisimo.services.DataProviderService
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.PullRequestToolWindowFactory
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.ListHelper
import sportisimo.utils.UIUtils
import java.awt.Color
import javax.swing.JPanel

class PullRequestThreadsTab(
    private val project: Project,
    private val connection: Connection
) : ATab()
{
    private val cachedData = ProjectDataState.getInstance(project)
    private val service = project.service<DataProviderService>()

    private val threadManager = ThreadingManager()

    private lateinit var threadsPanel: JPanel

    override fun getPanel(): JPanel
    {
        threadsPanel = UIUtils.createPanelWithBorderLayout(UIUtils.createLoadingPanel())

        val panel = PanelBuilder {
            indent {
                row {
                    column {
                        add(threadsPanel)
                    }
                }
            }
        }.build()

        registerEvents()

        service.getPullRequestsAsync()

        return UIUtils.createScrollablePanel(panel)
    }

    private fun registerEvents()
    {
        Events.subscribe(project, "PullRequestThreadsTab", Events.ON_PULL_REQUESTS_LOADED, object: IOnPullRequestsLoadedListener
        {
            override fun onChange(pullRequests: List<PullRequestData>)
            {
                onPullRequestsLoaded()
            }
        })

        Events.subscribe(project, "PullRequestThreadsTab", Events.ON_PULL_REQUEST_COMMENTS_LOADED, object: IOnPullRequestThreadsLoadedListener
        {
            override fun onChange(threads: List<PullRequestThreadData>)
            {
                onPullRequestCommentThreadsLoaded(threads)
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

        loadPullRequestComments()
    }

    private fun loadPullRequestComments() = threadManager.executeOnPooledThread("loadPullRequestComments") {
        val iterations = service.getPullRequestIterations()
        if(iterations.isEmpty()) return@executeOnPooledThread

        service.getPullRequestThreadsAsync(iterations.last())
    }

    private fun onPullRequestCommentThreadsLoaded(threads: List<PullRequestThreadData>)
    {
        if(threads.isEmpty())
        {
            noThreadsFound()
            return
        }

        val pullRequest = service.getPullRequest()!!
        val sortedThreads = threads.sortedWith(ListHelper.getThreadsSorter())
        val currentUser = service.getCurrentUser()

        val newContent = PanelBuilder {
            sortedThreads.forEach { thread ->
                row {
                    column {
                        add(UIUtils.createThreadCommentsPanel(project, cachedData.connectionData!!.project, thread, true, currentUser, connection, pullRequest, true))
                    }
                }
                verticalGap(20)
            }
        }.build()

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(threadsPanel, newContent)
        }
    }

    private fun noThreadsFound()
    {
        val newContent = panel {
            row {
                comment("No comments found")
            }

        }

        ThreadingManager.executeOnDispatchThreadAndAwaitResult {
            UIUtils.applyPanelToComponent(threadsPanel, newContent)
        }
    }

    override fun getName(): String = "Comments"
    override fun getDescription(): String =  "Comments"
    override fun getColor(): Color? = null
}