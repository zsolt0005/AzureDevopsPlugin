package sportisimo.data.azure.client

import sportisimo.data.azure.PullRequestCompletionOptions
import sportisimo.data.azure.SimpleCommitData

data class UpdatePullRequestData(
    var title: String? = null,
    var description: String? = null,
    var autoCompleteSetBy: AutoCompleteSetByData? = null,
    var targetRefName: String? = null,
    var completionOptions: PullRequestCompletionOptions? = null,
    var isDraft: Boolean? = null,
    var status: String? = null,
    var lastMergeSourceCommit: SimpleCommitData? = null
)

data class AutoCompleteSetByData(val id: String)
{
    companion object
    {
        const val GUID_EMPTY = "00000000-0000-0000-0000-000000000000"
    }
}