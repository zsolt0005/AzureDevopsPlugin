package sportisimo.data.azure.responses

import sportisimo.data.azure.RepositoryRefData

data class RepositoryRefsResponseData(
    val count: Int,
    val value: List<RepositoryRefData>
)