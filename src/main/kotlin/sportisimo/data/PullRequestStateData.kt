package sportisimo.data

data class PullRequestStateData(
    var abandonedPullRequests: Boolean = true,
    var makeReviewerFlagged: Boolean = false,
    var hideNotActiveComments: Boolean = false,
    var pullRequestAutoRefreshFrequency: Int = 10,
    var pullRequestThreadsAutoRefreshFrequency: Int = 5,
)