package sportisimo.services

import com.intellij.openapi.project.Project
import sportisimo.azure.Connection
import sportisimo.data.azure.*
import sportisimo.data.azure.client.*
import sportisimo.states.AppSettingsState
import sportisimo.threading.ThreadingManager
import sportisimo.ui.PullRequestToolWindowFactory
import sportisimo.utils.NotificationUtils
import sportisimo.utils.StringUtils

class DataLoaderService
{
    private val settings = AppSettingsState.getInstance()

    fun loadPullRequestsByRepositoryAndBranch(
        project: Project,
        connection: Connection,
        repositoryData: RepositoryData,
        branchData: RepositoryRefData,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val status = if(settings.pullRequestState.abandonedPullRequests) PullRequestData.STATUS_ALL else PullRequestData.STATUS_ACTIVE

            return@executeOnPooledThread connection.gitClient.getPullRequests(repositoryData, status).filter {
                it.sourceRefName == branchData.name
            }
        }.onFailure { loadFailed("pull requests", it, project) }

        return@executeOnPooledThread null
    }

    fun loadProjectTeams(
        project: Project,
        connection: Connection,
        azureProject: ProjectData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            return@executeOnPooledThread connection.coreClient.getProjectTeams(azureProject)
        }.onFailure { loadFailed("project teams", it, project) }

        return@executeOnPooledThread null
    }

    fun loadPullRequestWorkItems(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val pullRequestWorkItems = connection.gitClient.getPullRequestWorkItems(pullRequest)

            val workItems = mutableListOf<WorkItemData>()
            pullRequestWorkItems.forEach {
                val workItem = connection.workItemClient.getWorkItem(pullRequest.repository.project, it.id) ?: return@forEach
                workItems.add(workItem)
            }

            return@executeOnPooledThread workItems
        }.onFailure { loadFailed("pull request work items", it, project) }

        return@executeOnPooledThread null
    }

    fun loadPipelineRunByPullRequest(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val searchData = BuildRunsSearchData(
                1,
                "refs/pull/${pullRequest.pullRequestId}/merge",
                QueryOrder.QueueTimeDescending
            )

            val pipelineRuns = connection.buildClient.getRuns(pullRequest.repository.project, searchData)

            return@executeOnPooledThread if(pipelineRuns.isNotEmpty()) pipelineRuns.first() else null
        }.onFailure { loadFailed("pipeline run for pull request", it, project) }

        return@executeOnPooledThread null
    }

    fun loadBranchCommitsByPullRequest(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            return@executeOnPooledThread connection.gitClient.getBranchCommits(
                pullRequest.repository,
                StringUtils.cleanRepositoryRefName(pullRequest.targetRefName)
            )
        }.onFailure { loadFailed("target branch commits", it, project) }

        return@executeOnPooledThread null
    }

    fun loadGitItemByPathAndVersion(
        project: Project,
        connection: Connection,
        repository: RepositoryData,
        path: String,
        version: String
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val searchData = GitItemsSearchData(
                path,
                true,
                version,
                GitItemVersionType.Commit
            )

            return@executeOnPooledThread connection.gitClient.getItem(repository, searchData)
        }.onFailure { loadFailed("item by path and version", it, project) }

        return@executeOnPooledThread null
    }

    fun loadPullRequestIterations(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            return@executeOnPooledThread connection.gitClient.getPullRequestIterations(pullRequest.repository, pullRequest)
        }.onFailure { loadFailed("pull request iterations", it, project) }

        return@executeOnPooledThread null
    }

    fun loadCurrentUser(
        project: Project,
        connection: Connection
    ) = ThreadingManager.executeOnPooledThread{
        runCatching {
            return@executeOnPooledThread connection.commonClient.getAuthenticatedUserInformation()
        }.onFailure { loadFailed("current user", it, project) }

        return@executeOnPooledThread null
    }

    fun loadPullRequestThreads(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData,
        lastIteration: PullRequestIterationData
    ) = ThreadingManager.executeOnPooledThread{
        runCatching {
            val options = PullRequestThreadOptionsData(lastIteration.id, 0)

            return@executeOnPooledThread connection.gitClient.getPullRequestThreads(pullRequest.repository, pullRequest, options)
        }.onFailure { loadFailed("pull request threads", it, project) }

        return@executeOnPooledThread null
    }

    fun loadPullRequestIterationsComparison(
        project: Project,
        connection: Connection,
        pullRequest: PullRequestData,
        compareFrom: PullRequestIterationData
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val searchData = CompareIterationsSearchData(0, 2000, 0)

            return@executeOnPooledThread connection.gitClient.compareIterations(pullRequest.repository, pullRequest, compareFrom, searchData)
        }.onFailure { loadFailed("pull request iterations changes", it, project) }

        return@executeOnPooledThread null
    }

    fun loadWorkItemTypes(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
    ) = ThreadingManager.executeOnPooledThread{
        runCatching {
            return@executeOnPooledThread connection.workItemClient.getWorkItemTypes(azureProject)
        }.onFailure { loadFailed("work item types", it, project) }

        return@executeOnPooledThread null
    }

    fun loadWorkItems(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        searchData: WorkItemSearchData
    ) = ThreadingManager.executeOnPooledThread{
        runCatching {
            return@executeOnPooledThread connection.searchClient.searchWorkItems(azureProject, searchData)
        }.onFailure { loadFailed("work items", it, project) }

        return@executeOnPooledThread null
    }

    fun loadUsers(
        project: Project,
        connection: Connection,
        searchData: SubjectQuerySearchData
    ) = ThreadingManager.executeOnPooledThread{
        runCatching {
            return@executeOnPooledThread connection.graphClient.searchUsers(searchData)
        }.onFailure { loadFailed("users", it, project) }

        return@executeOnPooledThread null
    }

    private fun loadFailed(subject: String, exception: Throwable?, project: Project)
    {
        NotificationUtils.notify(
            "Failed to fetch $subject from Azure DevOps",
            exception?.message ?: "Unexpected error happened while fetching $subject from Azure REST API",
            project = project,
            isAllowed = settings.notificationStates.toolWindowInitialization
        )
        PullRequestToolWindowFactory.removeIfExists(project)
    }
}