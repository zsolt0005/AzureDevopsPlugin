package icons

import com.intellij.openapi.util.IconLoader

object CustomIcons
{
    @JvmField val Like = IconLoader.getIcon("/icons/common/like.png", javaClass)
    @JvmField val LikeOutlined = IconLoader.getIcon("/icons/common/like_outline.png", javaClass)

    @JvmField val AzureDevOps = IconLoader.getIcon("/icons/azure_devops_13x13.png", javaClass)
    @JvmField val AzureRepos = IconLoader.getIcon("/icons/azure_repos_13x13.png", javaClass)

    @JvmField val PullRequestNotVoted = IconLoader.getIcon("/icons/pullrequests/NotVoted.png", javaClass)
    @JvmField val PullRequestApproved = IconLoader.getIcon("/icons/pullrequests/Approved.png", javaClass)
    @JvmField val PullRequestWaitingForAuthor = IconLoader.getIcon("/icons/pullrequests/WaitingForAuthor.png", javaClass)
    @JvmField val PullRequestRejected = IconLoader.getIcon("/icons/pullrequests/Rejected.png", javaClass)

    @JvmField val PipelineSucceeded = IconLoader.getIcon("/icons/pipeline/Succeeded.png", javaClass)
    @JvmField val PipelineInProgress = IconLoader.getIcon("/icons/pipeline/InProgress.png", javaClass)
    @JvmField val PipelineFailed = IconLoader.getIcon("/icons/pipeline/Failed.png", javaClass)
}