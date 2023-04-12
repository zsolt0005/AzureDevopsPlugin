package sportisimo.events.listeners

import sportisimo.data.azure.BuildRunData

interface IOnPullRequestPipelineRunLoadedListener
{
    fun onChange(run: BuildRunData?)
}