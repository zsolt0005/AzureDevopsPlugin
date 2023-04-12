package sportisimo.data.cache

import sportisimo.data.azure.GitItemData

data class GitItemsCacheData(
    val values: MutableList<GitItemData> = mutableListOf()
)