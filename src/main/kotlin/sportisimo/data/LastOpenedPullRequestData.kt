package sportisimo.data

import sportisimo.data.azure.CommitData
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.PullRequestIterationData

data class LastOpenedPullRequestData(
    @Transient var pullRequest: PullRequestData? = null,
    @Transient var targetBranchCommits: List<CommitData>? = null,
    @Transient var iterations: List<PullRequestIterationData>? = null
)