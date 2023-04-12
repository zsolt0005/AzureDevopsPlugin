package sportisimo.data.azure.responses

import sportisimo.data.azure.ProjectData

data class ProjectsResponseData(
    val count: Int,
    val value: List<ProjectData>
)
