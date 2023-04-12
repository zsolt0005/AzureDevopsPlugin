package sportisimo.data.azure

import com.google.gson.annotations.SerializedName

data class WorkItemSearchResultFieldsData(
    @SerializedName("system.id") val id: String,
    @SerializedName("system.workitemtype") val workItemType: String,
    @SerializedName("system.title") val title: String,
    @SerializedName("system.assignedto") val assignedTo: String,
    @SerializedName("system.state") val state: String,
    @SerializedName("system.tags") val tags: String,
    @SerializedName("system.rev") val rev: String,
    @SerializedName("system.createddate") val createdDate: String,
    @SerializedName("system.changeddate") val changedDate: String
)