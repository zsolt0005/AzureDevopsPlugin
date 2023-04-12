package sportisimo.data.azure.responses

import sportisimo.data.azure.PullRequestIterationData

data class PullRequestIterationsResponseData(
    val value: List<PullRequestIterationData>,
    val count: Int
)