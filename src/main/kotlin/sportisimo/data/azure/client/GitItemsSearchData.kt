package sportisimo.data.azure.client

data class GitItemsSearchData(
    val path: String,
    val includeContent: Boolean = false,
    val version: String,
    val versionType: GitItemVersionType
): AParamData()
{
    override fun toString(): String
    {
        val builder = StringBuilder()

        addParam(builder, "path", path)
        addParam(builder, "includeContent", includeContent.toString())
        addParam(builder, "versionDescriptor.version", version)
        addParam(builder, "versionDescriptor.versionType", versionType.value)

        return builder.toString()
    }
}

enum class GitItemVersionType(val value: String)
{
    Branch("branch"),
    Commit("commit"),
    Tag("tag")
}