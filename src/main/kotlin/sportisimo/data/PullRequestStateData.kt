package sportisimo.data

data class PullRequestStateData(
    var abandonedPullRequests: Boolean = false,
    var makeReviewerFlagged: Boolean = true,
    var hideNotActiveComments: Boolean = false,
    var pullRequestAutoRefreshFrequency: Int = 10,
    var pullRequestThreadsAutoRefreshFrequency: Int = 5,
)