package sportisimo.data.azure.responses

import sportisimo.data.azure.WorkItemSearchResultData

data class WorkItemSearchResultResponseData(
    val count: Int,
    val results: List<WorkItemSearchResultData>?,
    val infoCode: Int
)