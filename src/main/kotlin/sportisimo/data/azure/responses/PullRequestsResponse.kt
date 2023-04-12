package sportisimo.data.azure.responses

import sportisimo.data.azure.PullRequestData

data class PullRequestsResponse(
    val count: Int,
    val value: List<PullRequestData>
)