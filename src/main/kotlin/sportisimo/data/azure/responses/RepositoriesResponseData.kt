package sportisimo.data.azure.responses

import sportisimo.data.azure.RepositoryData

data class RepositoriesResponseData(
    val count: Int,
    val value: List<RepositoryData>
)