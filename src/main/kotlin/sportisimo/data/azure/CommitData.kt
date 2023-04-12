package sportisimo.data.azure

data class CommitData(
    val commitId: String,
    val comment: String,
    val commentTruncated: Boolean,
    val url: String,
    val author: SimpleUserData,
    val committer: SimpleUserData,
    val changeCounts: CommitChangeCountsData? = null
)