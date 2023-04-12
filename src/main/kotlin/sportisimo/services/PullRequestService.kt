package sportisimo.services

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import sportisimo.azure.Connection
import sportisimo.data.azure.PullRequestCompletionOptions
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.client.AutoCompleteSetByData
import sportisimo.data.azure.client.UpdatePullRequestData
import sportisimo.data.azure.client.UpdatePullRequestReviewerData
import sportisimo.events.Events
import sportisimo.exceptions.PullRequestException
import sportisimo.states.AppSettingsState
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager

object PullRequestService
{
    private val settings = AppSettingsState.getInstance()

    fun updateVote(
        vote: Int,
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val service = project.service<DataProviderService>()

        val currentUser = service.getCurrentUser()
        val reviewerData = connection.gitClient.getPullRequestReviewer(saveData.repositoryData!!, pullRequest, currentUser) ?: return@executeOnPooledThread

        val updateData = UpdatePullRequestReviewerData(
            vote,
            settings.pullRequestState.makeReviewerFlagged,
            reviewerData.hasDeclined,
            reviewerData.isRequired
        )

        connection.gitClient.updatePullRequestReviewer(saveData.repositoryData!!, pullRequest, currentUser, updateData)

        BackgroundTaskUtil.syncPublisher(project, Events.ON_UPDATE_PULL_REQUEST_TOOL_WINDOW).onChange()
    }

    fun publish(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(isDraft = false)
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun setAutoComplete(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        completionOptions: PullRequestCompletionOptions,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val service = project.service<DataProviderService>()

        val currentUser = service.getCurrentUser()
        val updateData = UpdatePullRequestData(
            autoCompleteSetBy = AutoCompleteSetByData(currentUser.id),
            completionOptions = completionOptions
        )
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun cancelAutoComplete(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(autoCompleteSetBy = AutoCompleteSetByData(AutoCompleteSetByData.GUID_EMPTY))
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun complete(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(
            status = PullRequestData.STATUS_COMPLETED,
            lastMergeSourceCommit = pullRequest.lastMergeSourceCommit
        )

        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun markAsDraft(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(isDraft = true)
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun reactivate(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(status = PullRequestData.STATUS_ACTIVE)
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    fun abandon(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        project: Project
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestData(status = PullRequestData.STATUS_ABANDONED)
        updatePullRequest(connection, saveData, pullRequest, updateData, project)
    }

    private fun updatePullRequest(
        connection: Connection,
        saveData: ProjectDataState,
        pullRequest: PullRequestData,
        updateData: UpdatePullRequestData,
        project: Project
    )
    {
        try
        {
            connection.gitClient.updatePullRequest(saveData.connectionData!!.project, saveData.repositoryData!!, pullRequest, updateData)
        }
        catch (e: PullRequestException)
        {
            BackgroundTaskUtil.syncPublisher(project, Events.ON_UPDATE_PULL_REQUEST_FAILED).onChange(e.message ?: "Unexpected error occurred")
            return
        }

        BackgroundTaskUtil.syncPublisher(project, Events.ON_UPDATE_PULL_REQUEST_TOOL_WINDOW).onChange()
    }
}