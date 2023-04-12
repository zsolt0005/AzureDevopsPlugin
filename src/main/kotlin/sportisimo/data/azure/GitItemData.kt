package sportisimo.data.azure

import com.google.gson.annotations.SerializedName

data class GitItemData(
    val objectId: String,
    val gitObjectType: String,
    val commitId: String,
    val path: String,
    val content: String? = null,
    val url: String,
    @SerializedName("_links") val links: GitItemLinksData
)

data class GitItemLinksData(
    val self: LinkData,
    val repository: LinkData,
    val blob: LinkData
)