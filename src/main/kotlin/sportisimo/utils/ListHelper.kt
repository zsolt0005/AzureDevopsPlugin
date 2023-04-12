package sportisimo.utils

import sportisimo.data.azure.PullRequestThreadData
import sportisimo.data.azure.PullRequestThreadStatus
import sportisimo.data.azure.RepositoryRefData

/**
 * Helper functions for working with Lists.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object ListHelper
{
    /**
     * Creates a comparator for sorting a list of repository names.
     *
     * @return The Comparator.
     */
    fun getRepositorySorter(): Comparator<RepositoryRefData>
    {
        return compareBy(
            {
                val branchName = StringUtils.cleanRepositoryRefName(it.name)

                if(branchName == "master") 0
                else if (branchName == "devel") 1
                else if (branchName.startsWith("feature")) 2
                else if (branchName.startsWith("bug")) 3
                else 4
            },
            {
                StringUtils.cleanRepositoryRefName(it.name)
            }
        )
    }

    fun getThreadsSorter(): Comparator<PullRequestThreadData>
    {
        return compareBy {
            when (it.status)
            {
                PullRequestThreadStatus.Active.value -> 0
                PullRequestThreadStatus.Pending.value -> 1
                PullRequestThreadStatus.Unknown.value -> 2
                PullRequestThreadStatus.WontFix.value -> 3
                PullRequestThreadStatus.Resolved.value -> 4
                PullRequestThreadStatus.ByDesign.value -> 5
                PullRequestThreadStatus.Closed.value -> 6
                else -> 7
            }
        }
    }
}