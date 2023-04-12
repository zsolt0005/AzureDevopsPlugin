package sportisimo.data.azure

data class PullRequestThreadCommentData(
    val id: Int,
    val parentCommentId: Int,
    val author: UserData,
    val content: String? = null,
    val publishedDate: String,
    val lastUpdatedDate: String,
    val lastContentUpdatedDate: String,
    val commentType: String,
    val usersLiked: List<UserData>?
)