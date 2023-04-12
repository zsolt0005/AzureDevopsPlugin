package sportisimo.events.listeners

import sportisimo.data.azure.PullRequestData

interface IOnPullRequestsLoadedListener
{
    fun onChange(pullRequests: List<PullRequestData>)
}