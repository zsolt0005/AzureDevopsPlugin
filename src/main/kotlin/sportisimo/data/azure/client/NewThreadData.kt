package sportisimo.data.azure.client

import sportisimo.data.azure.PullRequestThreadStatus
import sportisimo.data.azure.ThreadContextData

data class NewThreadData(
    val threadContext: ThreadContextData,
    val status: String = PullRequestThreadStatus.Active.value,
    val comments: MutableList<NewPullRequestThreadCommentData> = mutableListOf()
)