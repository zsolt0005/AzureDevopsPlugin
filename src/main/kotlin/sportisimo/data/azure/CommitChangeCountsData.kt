package sportisimo.data.azure

import com.google.gson.annotations.SerializedName

data class CommitChangeCountsData(
    @SerializedName("Add") val add: Int,
    @SerializedName("Edit") val edit: Int,
    @SerializedName("Delete") val delete: Int
)