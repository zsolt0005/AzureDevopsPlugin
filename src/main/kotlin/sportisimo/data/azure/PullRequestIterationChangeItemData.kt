package sportisimo.data.azure

data class PullRequestIterationChangeItemData(
    val objectId: String,
    val originalObjectId: String,
    val path: String? = null,
)