package sportisimo.events.listeners

import sportisimo.data.azure.ProjectTeamData

interface IOnProjectTeamsLoadedListener
{
    fun onChange(teams: List<ProjectTeamData>)
}