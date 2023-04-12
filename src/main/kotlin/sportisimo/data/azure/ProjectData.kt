package sportisimo.data.azure

data class ProjectData(
    val id: String,
    val name: String,
    val url: String,
    val state: String,
    val revision: Int,
    val visibility: String,
    val lastUpdateTime: String
)
{
    override fun toString(): String
    {
        return name
    }
}
