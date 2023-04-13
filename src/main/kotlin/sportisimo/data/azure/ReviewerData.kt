package sportisimo.data.azure

import com.google.gson.annotations.SerializedName
import icons.CustomIcons
import java.util.concurrent.Future
import javax.swing.Icon

data class ReviewerData(
    val reviewerUrl: String,
    val vote: Int,
    val hasDeclined: Boolean,
    val isFlagged: Boolean,
    val displayName: String,
    val url: String,
    @SerializedName("_links") val links: CreatorLinksData,
    val id: String,
    val uniqueName: String,
    val imageUrl: String,
    val isRequired: Boolean = false
): AAsyncImageData()
{
    @Transient var asyncImageIcon: Future<Icon>? = null

    override fun getAsyncIcon(): Future<Icon>? = asyncImageIcon

    fun getVoteIcon(): Icon
    {
        return when(vote)
        {
            VOTE_APPROVED, VOTE_APPROVED_WITH_SUGGESTIONS -> CustomIcons.PullRequestApproved
            VOTE_WAITING_FOR_AUTHOR -> CustomIcons.PullRequestWaitingForAuthor
            VOTE_REJECT -> CustomIcons.PullRequestRejected
            else -> CustomIcons.PullRequestNotVoted
        }
    }

    companion object
    {
        const val VOTE_APPROVED = 10
        const val VOTE_APPROVED_WITH_SUGGESTIONS = 5
        const val VOTE_NOT_VOTED = 0
        const val VOTE_WAITING_FOR_AUTHOR = -5
        const val VOTE_REJECT = -10
    }
}
