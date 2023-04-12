package sportisimo.azure.clients

import com.google.gson.Gson
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.*
import sportisimo.data.azure.client.*
import sportisimo.data.azure.responses.*
import sportisimo.exceptions.EmptyResponseBodyException
import sportisimo.exceptions.PullRequestException
import java.io.IOException

class GitClient(private val connection: Connection)
{
    /**
     * Get all repositories for the given project.
     *
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getRepositories(project: ProjectData): List<RepositoryData>
    {
        val url = getBaseUrl(project.id) + "git/repositories"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.GitRepositories)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, RepositoriesResponseData::class.java)

        return responseObject.value
    }

    /**
     * Gets all the available branches of the repository.
     *
     * @param repository
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getBranches(repository: RepositoryData): List<RepositoryRefData>
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/" + repository.id + "/refs"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.GitBranchRefs)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, RepositoryRefsResponseData::class.java)

        return responseObject.value
    }

    /**
     * Get all pull requests for the given repository.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getPullRequests(repository: RepositoryData, status: String): List<PullRequestData>
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/" + repository.id + "/pullRequests?searchCriteria.status=$status"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.PullRequests)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, PullRequestsResponse::class.java)

        return responseObject.value
    }

    fun getPullRequestWorkItems(pullRequest: PullRequestData): List<PullRequestWorkItem>
    {
        val url = getBaseUrl() + "git/repositories/${pullRequest.repository.id}/pullRequests/${pullRequest.pullRequestId}/workItems"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.PullRequests)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, PullRequestWorkItemsResponseData::class.java)

        return responseObject.value
    }

    fun getPullRequestReviewer(repository: RepositoryData, pullRequest: PullRequestData, user: IdentityData): ReviewerData?
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/reviewers/${user.id}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.PullRequests)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return null
        }

        return Gson().fromJson(responseBody, ReviewerData::class.java)
    }

    fun getItem(repository: RepositoryData, searchData: GitItemsSearchData): GitItemData?
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/" + repository.id + "/items?${searchData}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.GitItems)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return null
        }

        return Gson().fromJson(responseBody, GitItemData::class.java)
    }

    fun getPullRequestIterations(repository: RepositoryData, pullRequest: PullRequestData): List<PullRequestIterationData>
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/iterations"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.CommitChanges)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, PullRequestIterationsResponseData::class.java)

        return responseObject.value
    }

    fun getBranchCommits(repository: RepositoryData, branch: String, limit: Int? = null): List<CommitData>
    {
        var url = getBaseUrl() + "git/repositories/${repository.id}/commits?branch=$branch"
        if(limit != null)
        {
            url += "&${"$"}top=$limit"
        }

        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)
        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.BranchCommits)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, CommitsResponseData::class.java)

        return responseObject.value
    }

    fun getPullRequestThreads(
        repository: RepositoryData,
        pullRequest: PullRequestData,
        options: PullRequestThreadOptionsData
    ): List<PullRequestThreadData>
    {
        val url = getBaseUrl() + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads?${options}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.PullRequestThreads)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, PullRequestThreadsResponseData::class.java)

        return responseObject.value
    }

    fun compareIterations(repository: RepositoryData, pullRequest: PullRequestData, iteration: PullRequestIterationData, searchData: CompareIterationsSearchData): List<PullRequestIterationChangeData>
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/iterations/${iteration.id}/changes?${searchData}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.PullRequestIterationChanges)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, PullRequestIterationChangesResponseData::class.java)

        return responseObject.changeEntries
    }

    /**
     * Creates a new pull request.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun createPullRequest(project: ProjectData, repository: RepositoryData, pullRequestData: NewPullRequestData) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests"

        val requestBody = Gson().toJson(pullRequestData)
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, requestBody)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.CreatePullRequest)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return
        }

        if(responseCode == 400)
        {
            val errorResponseObject = Gson().fromJson(responseBody, ErrorResponseData::class.java)
            throw PullRequestException(errorResponseObject.message)
        }
    }

    fun updatePullRequest(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        updateData: UpdatePullRequestData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}"

        val requestBody = Gson().toJson(updateData)
        val (responseCode, responseBody) = connection.doPatchRequestAndGetBodyAsString(url, requestBody)

        checkResponse(responseCode, responseBody, Scope.UpdatePullRequest)
    }

    fun updatePullRequestReviewer(
        repository: RepositoryData,
        pullRequest: PullRequestData,
        user: IdentityData,
        updateData: UpdatePullRequestReviewerData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(repository.project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/reviewers/${user.id}"

        val requestBody = Gson().toJson(updateData)
        val (responseCode, responseBody) = connection.doPutRequestAndGetBodyAsString(url, requestBody)

        checkResponse(responseCode, responseBody, Scope.UpdatePullRequestReviewer)
    }

    fun deletePullRequestThreadComment(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}/comments/${comment.id}"
        val (responseCode, responseBody) = connection.doDeleteRequestAndGetBodyAsString(url)

        checkResponse(responseCode, responseBody, Scope.DeletePullRequestThreadComment)
    }

    fun deletePullRequestThreadCommentLike(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}/comments/${comment.id}/likes"
        val (responseCode, responseBody) = connection.doDeleteRequestAndGetBodyAsString(url)

        checkResponse(responseCode, responseBody, Scope.UpdatePullRequestThreadComment)
    }

    fun addPullRequestThreadCommentLike(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}/comments/${comment.id}/likes"
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, "{}")

        checkResponse(responseCode, responseBody, Scope.UpdatePullRequestThreadComment)
    }

    fun addPullRequestThreadComment(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        commentContent: NewPullRequestThreadCommentData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}/comments"

        val requestBody = Gson().toJson(commentContent)
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, requestBody)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.CreatePullRequestThreadComment)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return
        }
    }

    fun createPullRequestThread(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        threadContent: NewThreadData
    ): PullRequestThreadData? // TODO UpdateProvider ?
    {
        val url =
            getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads"

        val requestBody = Gson().toJson(threadContent)
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, requestBody)

        if (responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.CreatePullRequestThread)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return null
        }

        return Gson().fromJson(responseBody, PullRequestThreadData::class.java)
    }

    fun updatePullRequestThreadComment(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData,
        commentContent: NewPullRequestThreadCommentData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}/comments/${comment.id}"

        val requestBody = Gson().toJson(commentContent)
        val (responseCode, responseBody) = connection.doPatchRequestAndGetBodyAsString(url, requestBody)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.UpdatePullRequestThreadComment)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return
        }
    }

    fun updatePullRequestThread(
        project: ProjectData,
        repository: RepositoryData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        updateData: UpdatePullRequestThreadData
    ) // TODO UpdateProvider ?
    {
        val url = getBaseUrl(project.id) + "git/repositories/${repository.id}/pullRequests/${pullRequest.pullRequestId}/threads/${thread.id}"

        val requestBody = Gson().toJson(updateData)
        val (responseCode, responseBody) = connection.doPatchRequestAndGetBodyAsString(url, requestBody)

        checkResponse(responseCode, responseBody, Scope.UpdatePullRequestThread)
    }

    private fun checkResponse(responseCode: Int, responseBody: String, scope: Scope)
    {
        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(scope)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return
        }

        if(responseCode == 400 || responseCode == 403)
        {
            val errorResponseObject = Gson().fromJson(responseBody, ErrorResponseData::class.java)
            throw PullRequestException(errorResponseObject.message)
        }
    }

    private fun getBaseUrl(project: String? = null): String
    {
        var baseUrl = "https://dev.azure.com/${connection.organization}/"

        if(project != null)
        {
            baseUrl += "$project/"
        }

        baseUrl += "_apis/"

        return baseUrl
    }
}