package sportisimo.services

import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import sportisimo.azure.Connection
import sportisimo.data.AvatarIcon
import sportisimo.data.ProjectTeamsData
import sportisimo.data.PullRequestsData
import sportisimo.data.WorkItemTypesData
import sportisimo.data.azure.*
import sportisimo.data.azure.client.*
import sportisimo.data.ui.AvatarIconOptionsData
import sportisimo.events.Events
import sportisimo.exceptions.NotFoundException
import sportisimo.exceptions.PullRequestException
import sportisimo.states.AppSettingsState
import sportisimo.states.ApplicationCache
import sportisimo.states.ProjectCache
import sportisimo.states.ProjectDataState
import sportisimo.threading.ThreadingManager
import sportisimo.utils.ImageUtils
import sportisimo.utils.StringUtils
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

class DataProviderService(private val project: Project)
{
    private val settings = AppSettingsState.getInstance()
    private val cachedData = ProjectDataState.getInstance(project)
    private val projectCache = ProjectCache.getInstance(project)
    private val applicationCache = ApplicationCache.getInstance()

    private val connection = Connection(cachedData.connectionData!!)

    fun getPullRequests(ignoreCache: Boolean = false): List<PullRequestData>
    {
        if(!ignoreCache && cachedData.pullRequests != null)
        {
            BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUESTS_LOADED).onChange(cachedData.pullRequests!!.values)
            return cachedData.pullRequests!!.values
        }

        val branchData = cachedData.branchData!!
        val repositoryData = cachedData.repositoryData!!
        val status = if(settings.pullRequestState.abandonedPullRequests) PullRequestData.STATUS_ALL else PullRequestData.STATUS_ACTIVE

        val pullRequests = connection.gitClient.getPullRequests(repositoryData, status).filter {
            it.sourceRefName == branchData.name
        }

        pullRequests.forEach {
            it.createdBy.asyncImageIcon = getAvatarAsync(it.createdBy.imageUrl).task!!

            it.autoCompleteSetBy?.asyncImageIcon = getAvatarAsync(it.autoCompleteSetBy!!.imageUrl).task!!

            it.reviewers.forEach { reviewer ->
                reviewer.asyncImageIcon = getAvatarAsync(reviewer.imageUrl).task!!
            }
        }

        cachedData.pullRequests = PullRequestsData(pullRequests)
        if(cachedData.lastOpenedPullRequest.pullRequest != null)
        {
            val pullRequestToUpdate = pullRequests.find { it.pullRequestId == cachedData.lastOpenedPullRequest.pullRequest!!.pullRequestId }
            if(pullRequestToUpdate != null) cachedData.lastOpenedPullRequest.pullRequest = pullRequestToUpdate
        }

        BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUESTS_LOADED).onChange(pullRequests)
        return pullRequests
    }

    fun getPullRequestsAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<PullRequestData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPullRequests(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getPullRequest(): PullRequestData?
    {
        return cachedData.lastOpenedPullRequest.pullRequest
    }

    fun getPullRequestWorkItems(): List<WorkItemData>
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!
        val pullRequestWorkItems = connection.gitClient.getPullRequestWorkItems(pullRequest)

        val workItems = mutableListOf<WorkItemData>()
        pullRequestWorkItems.forEach {
            val workItem = connection.workItemClient.getWorkItem(pullRequest.repository.project, it.id) ?: return@forEach
            workItems.add(workItem)
        }

        workItems.forEach { workItem ->
            workItem.svgIcon = ""

            val foundType = cachedData.workItemTypes?.values?.find {
                it.name == workItem.fields.workItemType
            } ?: return@forEach

            workItem.svgIcon = foundType.icon.svgIcon
        }

        BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUEST_WORK_ITEMS_LOADED).onChange(workItems)
        return workItems
    }

    fun getPullRequestWorkItemsAsync(
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<WorkItemData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPullRequestWorkItems()
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getPipelineRun(): BuildRunData?
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!

        val searchData = BuildRunsSearchData(
            1,
            "refs/pull/${pullRequest.pullRequestId}/merge",
            QueryOrder.QueueTimeDescending
        )

        val pipelineRuns = connection.buildClient.getRuns(pullRequest.repository.project, searchData)
        val pipelineRun = if(pipelineRuns.isNotEmpty()) pipelineRuns.first() else null

        BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUEST_PIPELINE_RUN_LOADED).onChange(pipelineRun)
        return pipelineRun
    }

    fun getPipelineRunAsync(
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((BuildRunData?) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPipelineRun()
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getTargetBranchCommits(ignoreCache: Boolean = false): List<CommitData>
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        if(!ignoreCache && cachedData.lastOpenedPullRequest.targetBranchCommits != null)
        {
            BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUEST_TARGET_BRANCH_COMMITS_LOADED).onChange(
                cachedData.lastOpenedPullRequest.targetBranchCommits!!)
            return cachedData.lastOpenedPullRequest.targetBranchCommits!!
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!

        val commits = connection.gitClient.getBranchCommits(
            pullRequest.repository,
            StringUtils.cleanRepositoryRefName(pullRequest.targetRefName)
        )

        cachedData.lastOpenedPullRequest.targetBranchCommits = commits
        BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUEST_TARGET_BRANCH_COMMITS_LOADED).onChange(commits)
        return commits
    }

    fun getTargetBranchCommitsAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<CommitData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getTargetBranchCommits(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getProjectTeams(ignoreCache: Boolean = false): List<ProjectTeamData>
    {
        if(!ignoreCache && cachedData.projectTeams != null)
        {
            BackgroundTaskUtil.syncPublisher(project, Events.ON_PROJECT_TEAMS_LOADED).onChange(cachedData.projectTeams!!.values)
            return cachedData.projectTeams!!.values
        }

        val teams = connection.coreClient.getProjectTeams(cachedData.connectionData!!.project)

        cachedData.projectTeams = ProjectTeamsData(teams)
        BackgroundTaskUtil.syncPublisher(project, Events.ON_PROJECT_TEAMS_LOADED).onChange(teams)
        return teams
    }

    fun getProjectTeamsAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<ProjectTeamData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getProjectTeams(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getGitItemByPathAndVersion(
        path: String,
        version: String
    ): GitItemData?
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        val cachedFile = projectCache.gitItems.values.firstOrNull {
            it.path == path && it.commitId == version
        }

        if(cachedFile != null)
        {
            return cachedFile
        }

        val searchData = GitItemsSearchData(
            path,
            true,
            version,
            GitItemVersionType.Commit
        )

        val repository = cachedData.lastOpenedPullRequest.pullRequest!!.repository
        val item = connection.gitClient.getItem(repository, searchData) ?: return null

        projectCache.gitItems.values.add(item)
        return item
    }

    fun getGitItemByPathAndVersionAsync(
        path: String,
        version: String,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((GitItemData?) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getGitItemByPathAndVersion(path, version)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getPullRequestIterations(ignoreCache: Boolean = false): List<PullRequestIterationData>
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        if(!ignoreCache && cachedData.lastOpenedPullRequest.iterations != null)
        {
            return cachedData.lastOpenedPullRequest.iterations!!
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!
        val iterations = connection.gitClient.getPullRequestIterations(pullRequest.repository, pullRequest)

        cachedData.lastOpenedPullRequest.iterations = iterations
        return iterations
    }

    fun getPullRequestIterationsAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<PullRequestIterationData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPullRequestIterations(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getCurrentUser(ignoreCache: Boolean = false): IdentityData
    {
        if(!ignoreCache && cachedData.currentUser != null)
        {
            return cachedData.currentUser!!
        }

        val currentUser = connection.commonClient.getAuthenticatedUserInformation() ?: throw NotFoundException("Current user wah not found")

        currentUser.asyncImageIcon = getAvatarAsync(currentUser.subjectDescriptor, "large").task!!

        cachedData.currentUser = currentUser
        return currentUser
    }

    fun getCurrentUserAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((IdentityData) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getCurrentUser(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getPullRequestThreads(lastIteration: PullRequestIterationData): List<PullRequestThreadData>
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!

        val options = PullRequestThreadOptionsData(lastIteration.id, 0)
        val threads = connection.gitClient.getPullRequestThreads(pullRequest.repository, pullRequest, options)

        val filteredThreads = threads.filter {
            it.threadContext != null
            && !it.isDeleted
            // && !it.comments.any { comment -> comment.commentType == "system" }
        }

        filteredThreads.forEach { thread ->
            thread.comments.forEach { comment ->
                comment.author.asyncImageIcon = getAvatarAsync(comment.author.descriptor, "large").task!!
            }
        }

        BackgroundTaskUtil.syncPublisher(project, Events.ON_PULL_REQUEST_COMMENTS_LOADED).onChange(filteredThreads)
        return filteredThreads
    }

    fun getPullRequestThreadsAsync(
        lastIteration: PullRequestIterationData,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<PullRequestThreadData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPullRequestThreads(lastIteration)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getPullRequestIterationsComparison(iteration: PullRequestIterationData): List<PullRequestIterationChangeData>
    {
        if(cachedData.lastOpenedPullRequest.pullRequest == null)
        {
            throw PullRequestException("Failed to load the opened pull request")
        }

        val pullRequest = cachedData.lastOpenedPullRequest.pullRequest!!

        val searchData = CompareIterationsSearchData(0, 2000, 0)
        return connection.gitClient.compareIterations(pullRequest.repository, pullRequest, iteration, searchData)
    }

    fun getPullRequestIterationsComparisonAsync(
        iteration: PullRequestIterationData,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<PullRequestIterationChangeData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getPullRequestIterationsComparison(iteration)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getWorkItemTypes(ignoreCache: Boolean = false): List<WorkItemTypeData>
    {
        if(!ignoreCache && cachedData.workItemTypes != null)
        {
            return cachedData.workItemTypes!!.values
        }

        val workItemTypes = connection.workItemClient.getWorkItemTypes(cachedData.connectionData!!.project)

        cachedData.workItemTypes = WorkItemTypesData(workItemTypes)
        return workItemTypes
    }

    fun getWorkItemTypesAsync(
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<WorkItemTypeData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getWorkItemTypes(ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getWorkItems(searchData: WorkItemSearchData, ignoreCache: Boolean = false): List<WorkItemSearchResultData>
    {
        val workItems = connection.searchClient.searchWorkItems(cachedData.connectionData!!.project, searchData)

        var workItemTypes: List<WorkItemTypeData> = listOf()
        runCatching { workItemTypes = getWorkItemTypes(ignoreCache) }

        workItems.forEach { workItem ->
            val workItemType = workItemTypes.find {
                it.name == workItem.fields.workItemType
            }

            workItem.svgIcon = workItemType?.icon?.svgIcon ?: ""
        }

        return workItems
    }

    fun getWorkItemsAsync(
        searchData: WorkItemSearchData,
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<WorkItemSearchResultData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getWorkItems(searchData, ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getUsers(searchData: SubjectQuerySearchData, ignoreCache: Boolean = false): List<SubjectQueryResultData>
    {
        val users = connection.graphClient.searchUsers(searchData)

        users.forEach {
            it.avatar = getAvatarAsync(it.descriptor, "large", ignoreCache = ignoreCache).task!!
        }

        return users
    }

    fun getUsersAsync(
        searchData: SubjectQuerySearchData,
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((List<SubjectQueryResultData>) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getUsers(searchData, ignoreCache)
            onLoaded?.let { it(result) }

            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getAvatar(
        subjectDescriptor: String,
        size: String = "medium",
        options: AvatarIconOptionsData = AvatarIconOptionsData(20),
        ignoreCache: Boolean = false
    ): Icon
    {
        val cachedImage = getAvatarFromCache(subjectDescriptor, null, options, ignoreCache)
        if(cachedImage != null)
        {
            return cachedImage
        }

        val avatarBufferedImage = connection.graphClient.getSubjectAvatarAsBufferedImage(subjectDescriptor, size)

        return getAvatarFromBufferedImageAndCacheResult(avatarBufferedImage, subjectDescriptor, null, options)
    }

    fun getAvatarAsync(
        subjectDescriptor: String,
        size: String = "medium",
        options: AvatarIconOptionsData = AvatarIconOptionsData(20),
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((Icon) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getAvatar(subjectDescriptor, size, options, ignoreCache)
            onLoaded?.let { it(result) }
            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    fun getAvatar(
        url: String,
        options: AvatarIconOptionsData = AvatarIconOptionsData(20),
        ignoreCache: Boolean = false
    ): Icon
    {
        val cachedImage = getAvatarFromCache(null, url, options, ignoreCache)
        if(cachedImage != null)
        {
            return cachedImage
        }

        val avatarBufferedImage = connection.commonClient.getAvatarAsBufferedImage(url)

        return getAvatarFromBufferedImageAndCacheResult(avatarBufferedImage, null, url, options)
    }

    fun getAvatarAsync(
        url: String,
        options: AvatarIconOptionsData = AvatarIconOptionsData(20),
        ignoreCache: Boolean = false,
        onFailed: ((Throwable) -> Unit)? = null,
        onLoaded: ((Icon) -> Unit)? = null,
    ) = ThreadingManager.executeOnPooledThread {
        runCatching {
            val result = getAvatar(url, options, ignoreCache)
            onLoaded?.let { it(result) }
            return@executeOnPooledThread result
        }.onFailure { exception -> onFailed?.let { it(exception) } }

        return@executeOnPooledThread null
    }

    // Helpers

    private fun getAvatarFromCache(
        subjectDescriptor: String?,
        url: String?,
        options: AvatarIconOptionsData,
        ignoreCache: Boolean
    ): Icon?
    {
        val cachedImage = applicationCache.avatarIcons.values.find {
            (subjectDescriptor != null && it.subjectDescriptor == subjectDescriptor)
            || (url != null && it.url == url)
        }

        if(!ignoreCache && cachedImage != null)
        {
            val image = ImageUtils.base64StringToBufferedImage(cachedImage.base64Image)
            val resizedImage = ImageUtils.circularClipMask(image, options)
            return ImageIcon(resizedImage)
        }

        return null
    }

    private fun getAvatarFromBufferedImageAndCacheResult(
        avatarBufferedImage: BufferedImage,
        subjectDescriptor: String?,
        url: String?,
        options: AvatarIconOptionsData,
    ): Icon
    {
        val base64Image = ImageUtils.bufferedImageToBase64String(avatarBufferedImage)
        val cachedImageByBase64String = applicationCache.avatarIcons.values.find { it.base64Image == base64Image }

        if(cachedImageByBase64String != null)
        {
            cachedImageByBase64String.url = url ?: cachedImageByBase64String.url
            cachedImageByBase64String.subjectDescriptor = subjectDescriptor ?: cachedImageByBase64String.subjectDescriptor
        }
        else
        {
            val avatarIcon = AvatarIcon(url, subjectDescriptor, base64Image)
            applicationCache.avatarIcons.values.add(avatarIcon)
        }

        val resizedImage = ImageUtils.circularClipMask(avatarBufferedImage, options)
        return ImageIcon(resizedImage)
    }
}