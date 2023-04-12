package sportisimo.data

import sportisimo.data.azure.ProjectData

data class ConnectionData(
    val organization: String,
    val token: String,
    val project: ProjectData
)
{
    override fun toString(): String
    {
        return "$organization {${project.name}}"
    }
}