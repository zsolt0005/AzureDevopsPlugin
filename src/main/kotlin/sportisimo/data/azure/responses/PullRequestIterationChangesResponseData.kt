package sportisimo.data.azure.responses

import sportisimo.data.azure.PullRequestIterationChangeData

data class PullRequestIterationChangesResponseData(
    val changeEntries: List<PullRequestIterationChangeData>
)