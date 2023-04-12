package sportisimo.data.azure

data class CompletionOptionsData(
    val mergeCommitMessage: String,
    val deleteSourceBranch: Boolean,
    val squashMerge: Boolean,
    val mergeStrategy: String,
    val transitionWorkItems: Boolean
)