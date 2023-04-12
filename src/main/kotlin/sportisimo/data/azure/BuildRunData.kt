package sportisimo.data.azure

import com.google.gson.annotations.SerializedName
import icons.CustomIcons
import javax.swing.Icon

data class BuildRunData(
    @SerializedName("_links") val links: BuildRunLinksData,
    val properties: Any,
    val tags: Any,
    val validationResults: Any,
    val plans: Any,
    val triggerInfo: Any,
    val id: Int,
    val buildNumber: String,
    val status: String,
    val result: String?,
    val queueTime: String,
    val startTime: String?,
    val finishTime: String?,
    val url: String,
    val definition: Any,
    val buildNumberRevision: Int,
    val project: ProjectData,
    val uri: String,
    val sourceBranch: String,
    val sourceVersion: String,
    val queue: Any,
    val priority: String,
    val reason: String,
    val requestedFor: UserData,
    val requestedBy: UserData,
    val lastChangedDate: String,
    val lastChangedBy: UserData,
    val parameters: String,
    val repository: RepositoryData,
    val retainedByRelease: Boolean,
    val triggeredByBuild: Any,
    val appendCommitMessageToRunName: Boolean,
)
{
    companion object
    {
        const val RESULT_FAILED = "failed"
        const val RESULT_SUCCEEDED = "succeeded"

        const val STATUS_NOT_STARTED = "notStarted"
        const val STATUS_IN_PROGRESS = "inProgress"
        const val STATUS_COMPLETED = "completed"
    }

    fun getResultIcon(): Icon
    {
        if(status == STATUS_IN_PROGRESS) return CustomIcons.PipelineInProgress
        if(status == STATUS_NOT_STARTED) return CustomIcons.PipelineInProgress
        if(status == STATUS_COMPLETED && result == RESULT_SUCCEEDED) return CustomIcons.PipelineSucceeded
        if(status == STATUS_COMPLETED && result == RESULT_FAILED) return CustomIcons.PipelineFailed

        return CustomIcons.PipelineFailed
    }
}

data class BuildRunLinksData(
    val self: LinkData,
    val web: LinkData,
    val sourceVersionDisplayUri: LinkData,
    val timeline: LinkData,
    val badge: LinkData
)