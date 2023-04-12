package sportisimo.data.azure

data class CommitChangeItemData(
    val objectId: String,
    val originalObjectId: String,
    val gitObjectType: String,
    val commitId: String,
    val path: String,
    val isFolder: Boolean? = null,
    val url: String
)
{
    companion object
    {
        const val GIT_OBJECT_TYPE_BLOB = "blob"
        const val GIT_OBJECT_TYPE_TREE = "tree"
    }
}