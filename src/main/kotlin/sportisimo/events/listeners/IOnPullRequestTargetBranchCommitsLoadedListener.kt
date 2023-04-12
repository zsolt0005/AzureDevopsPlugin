package sportisimo.events.listeners

import sportisimo.data.azure.CommitData

interface IOnPullRequestTargetBranchCommitsLoadedListener
{
    fun onChange(pullRequests: List<CommitData>)
}