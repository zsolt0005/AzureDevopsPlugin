package sportisimo.data.azure

data class PullRequestCompletionOptions(
   var deleteSourceBranch: Boolean? = null,
   var transitionWorkItems: Boolean? = null,
   var mergeStrategy: String? = null,
   val squashMerge: Boolean? = null
)
{
    companion object
    {
        const val MERGE_STRATEGY_NO_FAST_FORWARD = "noFastForward"
        const val MERGE_STRATEGY_REBASE = "rebase"
        const val MERGE_STRATEGY_REBASE_MERGE = "rebaseMerge"
        const val MERGE_STRATEGY_SQUASH = "squash"
    }
}