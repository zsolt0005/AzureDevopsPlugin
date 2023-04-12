package sportisimo.ui.tabs.toolwindow

import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import sportisimo.data.azure.PullRequestData
import sportisimo.data.azure.ReviewerData
import sportisimo.ui.builders.PanelBuilder
import javax.swing.JPanel

abstract class ATab: ITab
{
    protected fun getReviewersPanel(pullRequest: PullRequestData): JPanel
    {
        val requiredReviewers = pullRequest.reviewers.filter { it.isRequired }
        val optionalReviewers = pullRequest.reviewers.filter { !it.isRequired }

        return panel {
            if(requiredReviewers.isNotEmpty())
            {
                row { comment("Required") }
                indent { row { cell(getReviewerGroup(requiredReviewers)) } }
            }

            if(optionalReviewers.isNotEmpty())
            {
                row { comment("Optional") }
                indent { row { cell(getReviewerGroup(optionalReviewers)) } }
            }
        }
    }

    private fun getReviewerGroup(reviewersData: List<ReviewerData>): DialogPanel
    {
        return panel {
            reviewersData.forEach { reviewerData ->
                row {
                    icon(reviewerData.getVoteIcon())

                    runCatching {
                        cell(
                            PanelBuilder { row { column { asyncIcon(reviewerData.getImageIconAsync()) } }}.build()
                        ).gap(RightGap.SMALL)
                    }

                    text("<b>${reviewerData.displayName}</b>")
                }
            }
        }
    }
}