package sportisimo.data.azure

data class CommitChangeData(
    val item: CommitChangeItemData,
    val sourceServerItem: String? = null,
    val changeType: String
)