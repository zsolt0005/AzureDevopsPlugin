package sportisimo.data.azure.responses

import sportisimo.data.azure.PullRequestThreadData

data class PullRequestThreadsResponseData(
    val count: Int,
    val value: List<PullRequestThreadData>
)