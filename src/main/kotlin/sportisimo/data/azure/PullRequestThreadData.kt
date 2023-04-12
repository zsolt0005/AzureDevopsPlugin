package sportisimo.data.azure

import com.jetbrains.rd.util.first

data class PullRequestThreadData(
    val id: Int,
    val publishedDate: String,
    val lastUpdatedDate: String,
    val comments: List<PullRequestThreadCommentData>,
    val status: String? = null,
    val threadContext: ThreadContextData?,
    val isDeleted: Boolean
)
{
    fun getDisplayStatus(): String
    {
        if(status == null || !statusToDisplayStatusMap.containsKey(status)) return "Unknown"

        return statusToDisplayStatusMap[status]!!
    }

    fun isActive() = (status == PullRequestThreadStatus.Active.value || status == PullRequestThreadStatus.Pending.value)

    companion object
    {
        val statusToDisplayStatusMap = mapOf(
            PullRequestThreadStatus.Active.value to "Active",
            PullRequestThreadStatus.Closed.value to "Closed",
            PullRequestThreadStatus.Resolved.value to "Resolved",
            PullRequestThreadStatus.Pending.value to "Pending",
            PullRequestThreadStatus.Unknown.value to "Unknown",
            PullRequestThreadStatus.ByDesign.value to "Unknown", // Must be after Unknown
            PullRequestThreadStatus.WontFix.value to "Won't fix",
        )

        private fun getDisplayStatusByStatus(status: String): String = statusToDisplayStatusMap[status] ?: "Unknown"

        fun getDisplayStatuses(): List<String> = listOf(
            getDisplayStatusByStatus(PullRequestThreadStatus.Active.value),
            getDisplayStatusByStatus(PullRequestThreadStatus.Pending.value),
            getDisplayStatusByStatus(PullRequestThreadStatus.Resolved.value),
            getDisplayStatusByStatus(PullRequestThreadStatus.WontFix.value),
            getDisplayStatusByStatus(PullRequestThreadStatus.Closed.value)
        )

        fun getStatusFromDisplayStatus(displayStatus: String) = statusToDisplayStatusMap.filter { it.value == displayStatus }.first().key
    }
}

enum class PullRequestThreadStatus(val value: String)
{
    Active("active"),
    ByDesign("byDesign"),
    Closed("closed"),
    Resolved("fixed"),
    Pending("pending"),
    Unknown("unknown"),
    WontFix("wontFix")
}