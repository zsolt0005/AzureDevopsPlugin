package sportisimo.data.azure

data class PullRequestIterationChangeData(
    val changeTrackingId: Int,
    val changeId: Int,
    val originalPath: String? = null,
    val item: PullRequestIterationChangeItemData,
    val changeType: String,
)