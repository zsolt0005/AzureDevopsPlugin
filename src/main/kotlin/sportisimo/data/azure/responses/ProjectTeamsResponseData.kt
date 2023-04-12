package sportisimo.data.azure.responses

import sportisimo.data.azure.ProjectTeamData

data class ProjectTeamsResponseData(
    val count: Int,
    val value: List<ProjectTeamData>
)