package sportisimo.events.listeners

import sportisimo.data.azure.PullRequestThreadData

interface IOnPullRequestThreadsLoadedListener
{
    fun onChange(threads: List<PullRequestThreadData>)
}