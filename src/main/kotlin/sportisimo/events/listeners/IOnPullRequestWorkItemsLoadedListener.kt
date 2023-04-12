package sportisimo.events.listeners

import sportisimo.data.azure.WorkItemData

interface IOnPullRequestWorkItemsLoadedListener
{
    fun onChange(workItems: List<WorkItemData>)
}