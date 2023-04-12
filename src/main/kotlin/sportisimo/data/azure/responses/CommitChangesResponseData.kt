package sportisimo.data.azure.responses

import sportisimo.data.azure.CommitChangeCountsData
import sportisimo.data.azure.CommitChangeData

data class CommitChangesResponseData(
    val changeCounts: CommitChangeCountsData,
    val changes: List<CommitChangeData>
)