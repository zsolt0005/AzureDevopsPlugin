package sportisimo.data.azure.responses

import sportisimo.data.azure.PullRequestWorkItem

data class PullRequestWorkItemsResponseData(
    val count: Int,
    val value: List<PullRequestWorkItem>
)