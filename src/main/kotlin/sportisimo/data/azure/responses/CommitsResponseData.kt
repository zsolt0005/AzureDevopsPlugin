package sportisimo.data.azure.responses

import sportisimo.data.azure.CommitData

data class CommitsResponseData(
    val count: Int,
    val value: List<CommitData>
)