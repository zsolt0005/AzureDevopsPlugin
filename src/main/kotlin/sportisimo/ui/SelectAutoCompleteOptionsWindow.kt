package sportisimo.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.util.ui.JBUI
import sportisimo.data.azure.PullRequestCompletionOptions
import sportisimo.ui.builders.PanelBuilder
import javax.swing.JComponent

class SelectAutoCompleteOptionsWindow(
    private val branchName: String,
    private val completionOptions: PullRequestCompletionOptions
): DialogWrapper(true)
{
    init
    {
        title = "Automatic Complete Options"
        init()
    }

    private lateinit var completeWorkItemsCheckBox: JBCheckBox
    private lateinit var deleteSourceCheckBox: JBCheckBox
    private lateinit var mergeTypeComboBox: ComboBox<String>

    override fun createCenterPanel(): JComponent
    {
        val panel = PanelBuilder {
            preferredSize(500, 0)
            border(JBUI.Borders.empty(8))

            row { column { boldLabel("Merge type") } }
            row { column { mergeTypeComboBox = comboBox(listOf(
                PullRequestCompletionOptions.MERGE_STRATEGY_SQUASH,
                PullRequestCompletionOptions.MERGE_STRATEGY_NO_FAST_FORWARD,
                PullRequestCompletionOptions.MERGE_STRATEGY_REBASE_MERGE,
                PullRequestCompletionOptions.MERGE_STRATEGY_REBASE,
            )) } }

            verticalGap(20)
            row { column { boldLabel("Post-completion options") } }
            row { column { completeWorkItemsCheckBox = checkBox("Complete associated work items after merging") } }
            row { column { deleteSourceCheckBox = checkBox("Delete $branchName after merging") } }
        }.build()

        // Default values
        completeWorkItemsCheckBox.isSelected = true
        deleteSourceCheckBox.isSelected = true

        return panel
    }


    override fun doOKAction()
    {
        completionOptions.deleteSourceBranch = deleteSourceCheckBox.isSelected
        completionOptions.transitionWorkItems = completeWorkItemsCheckBox.isSelected
        completionOptions.mergeStrategy = mergeTypeComboBox.selectedItem as String

        super.doOKAction()
    }
}