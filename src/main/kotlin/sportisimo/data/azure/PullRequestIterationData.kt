package sportisimo.data.azure

data class PullRequestIterationData(
    val id: Int,
    val description: String,
    val author: UserData,
    val createdDate: String,
    val updatedDate: String,
    val sourceRefCommit: SimpleCommitData,
    val targetRefCommit: SimpleCommitData,
    val commonRefCommit: SimpleCommitData,
    val hasMoreCommits: Boolean,
    val reason: String,
)