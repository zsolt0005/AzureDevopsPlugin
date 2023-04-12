package sportisimo.data.azure.client

data class NewPullRequestData(
    val sourceRefName: String,
    val targetRefName: String,
    val title: String,
    val description: String,
    val isDraft: Boolean,
    val reviewers: List<NewPullRequestReviewerData>,
    val workItemRefs: List<NewPullRequestResourceRef>
)

data class NewPullRequestReviewerData(val id: String, val isRequired: Boolean)

data class NewPullRequestResourceRef(val id: String, val url: String)