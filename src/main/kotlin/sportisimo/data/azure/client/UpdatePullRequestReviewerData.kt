package sportisimo.data.azure.client

data class UpdatePullRequestReviewerData(
    val vote: Int,
    val isFlagged: Boolean,
    val hasDeclined: Boolean,
    val isRequired: Boolean
)