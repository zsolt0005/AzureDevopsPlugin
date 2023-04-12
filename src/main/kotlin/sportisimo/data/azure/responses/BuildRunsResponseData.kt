package sportisimo.data.azure.responses

import sportisimo.data.azure.BuildRunData

data class BuildRunsResponseData(
    val count: Int,
    val value: List<BuildRunData>
)