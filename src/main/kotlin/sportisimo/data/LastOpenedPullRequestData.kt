package sportisimo.data

import sportisimo.data.azure.CommitData
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.PullRequestIterationData

data class LastOpenedPullRequestData(
    var pullRequest: PullRequestData,
    var targetBranchCommits: List<CommitData>? = null,
    var iterations: List<PullRequestIterationData>? = null
)