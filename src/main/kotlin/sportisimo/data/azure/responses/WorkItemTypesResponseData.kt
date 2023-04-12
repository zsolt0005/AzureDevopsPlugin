package sportisimo.data.azure.responses

import sportisimo.data.azure.WorkItemTypeData

data class WorkItemTypesResponseData(
    val count: Int,
    val value: List<WorkItemTypeData>
)