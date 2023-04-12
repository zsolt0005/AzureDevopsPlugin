package sportisimo.data.azure

import sportisimo.utils.StringUtils

data class RepositoryRefData(
    val name: String,
    val objectId: String,
    val creator: UserData,
    val url: String
)
{
    override fun toString(): String
    {
        return StringUtils.cleanRepositoryRefName(name)
    }
}