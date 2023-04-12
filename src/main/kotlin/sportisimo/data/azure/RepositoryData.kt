package sportisimo.data.azure

data class RepositoryData(
    val id: String,
    val name: String,
    val url: String,
    val project: ProjectData,
    val defaultBranch: String,
    val size: Int,
    val remoteUrl: String,
    val sshUrl: String,
    val webUrl: String,
    val isDisabled: Boolean,
    val isInMaintenance: Boolean,
)
{
    override fun toString(): String
    {
        return name
    }
}