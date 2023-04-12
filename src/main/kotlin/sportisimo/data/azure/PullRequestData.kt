package sportisimo.data.azure

data class PullRequestData(
    val repository: RepositoryData,
    val pullRequestId: Int,
    val codeReviewId: Int,
    val status: String,
    val createdBy: UserData,
    val creationDate: String,
    val title: String,
    val sourceRefName: String,
    val targetRefName: String,
    val mergeStatus: String,
    val isDraft: Boolean,
    val mergeId: String,
    val lastMergeSourceCommit: SimpleCommitData,
    val lastMergeTargetCommit: SimpleCommitData,
    val lastMergeCommit: SimpleCommitData,
    val reviewers: List<ReviewerData>,
    val url: String,
    val supportsIterations: Boolean,
    val completionOptions: CompletionOptionsData? = null,
    val autoCompleteSetBy: UserData? = null
)
{
    companion object
    {
        const val STATUS_ALL = "all"
        const val STATUS_ACTIVE = "active"
        const val STATUS_COMPLETED = "completed"
        const val STATUS_NOT_SET = "notSet"
        const val STATUS_ABANDONED = "abandoned"
    }
}