package sportisimo.data.azure

import com.google.gson.annotations.SerializedName

data class WorkItemData(
    val id: Int,
    val rev: Int,
    val fields: WorkItemFieldsData,
    @SerializedName("_links") val links: WorkItemLinksData,
    val url: String,
    var svgIcon: String
)

data class WorkItemLinksData(
    val self: LinkData,
    val workItemUpdates: LinkData,
    val workItemRevisions: LinkData,
    val workItemComments: LinkData,
    val html: LinkData,
    val workItemType: LinkData,
    val fields: LinkData,
)

data class WorkItemFieldsData(
    @SerializedName("System.AreaPath") val areaPath: String,
    @SerializedName("System.TeamProject") val teamProject: String,
    @SerializedName("System.IterationPath") val iterationPath: String,
    @SerializedName("System.WorkItemType") val workItemType: String,
    @SerializedName("System.State") val state: String,
    @SerializedName("System.Reason") val reason: String,
    @SerializedName("System.AssignedTo") val assignedTo: UserData,
    @SerializedName("System.CreatedDate") val createdDate: String,
    @SerializedName("System.CreatedBy") val createdBy: UserData,
    @SerializedName("System.ChangedDate") val changedDate: String,
    @SerializedName("System.ChangedBy") val changedBy: UserData,
    @SerializedName("System.CommentCount") val commentCount: Int,
    @SerializedName("System.Title") val title: String,
    @SerializedName("Microsoft.VSTS.Common.StateChangeDate") val stateChangedDate: String,
    @SerializedName("System.Description") val description: String
)