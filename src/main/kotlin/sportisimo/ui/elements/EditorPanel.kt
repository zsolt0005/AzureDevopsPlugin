package sportisimo.ui.elements

import com.intellij.ui.components.JBTextArea
import sportisimo.data.builders.ColumnAlignment
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.EventUtils
import java.awt.BorderLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.event.DocumentEvent

class EditorPanel(
    private val mainButtonText: String,
    private val centerButtonActions: List<JButton> = listOf()
): JPanel()
{
    private lateinit var textArea: JBTextArea
    private lateinit var bottomPanel: JPanel

    private val onTextAreaUpdatedCallbacks = mutableListOf<((DocumentEvent?) -> Unit)>()
    private val onReplyClickedCallbacks = mutableListOf<((String) -> Unit)>()
    private val onCancelClickedCallbacks = mutableListOf<(() -> Unit)>()

    init {
        this.layout = BorderLayout()
        render()
    }

    private fun render()
    {
        val content = PanelBuilder {
            row {
                column {
                    textArea = expandableTextArea { e -> onTextAreaUpdated(e) }

                    textArea.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
                        onMouseClicked = { showBottomPanel() }
                    ))
                }
            }
            row {
                column(ColumnAlignment.Right) {
                    button("Cancel") {
                        onCancelClicked()
                        cancelBottomPanel()
                    }
                    centerButtonActions.forEach {
                        add(it)
                    }
                    button(mainButtonText) {
                        onReplyClicked(textArea.text)
                        cancelBottomPanel()
                    }
                }

                bottomPanel = this.rowPanel
                bottomPanel.isVisible = false
            }
            emptyRow()
        }.build()

        add(content)
    }

    fun showBottomPanel() {
        bottomPanel.isVisible = true
        revalidate()

        onTextAreaUpdated(null)
    }

    private fun cancelBottomPanel() {
        textArea.text = ""
        bottomPanel.isVisible = false
        revalidate()

        onTextAreaUpdated(null)
    }

    private fun onTextAreaUpdated(e: DocumentEvent?)
    {
        onTextAreaUpdatedCallbacks.forEach { it(e) }
    }

    private fun onReplyClicked(text: String)
    {
        onReplyClickedCallbacks.forEach { it(text) }
    }

    private fun onCancelClicked()
    {
        onCancelClickedCallbacks.forEach { it() }
    }

    fun addOnTextAreaUpdated(cb: (DocumentEvent?) -> Unit)
    {
        onTextAreaUpdatedCallbacks.add(cb)
    }

    fun addOnReplyClicked(cb: (String) -> Unit)
    {
        onReplyClickedCallbacks.add(cb)
    }

    fun addOnCancelClicked(cb: () -> Unit)
    {
        onCancelClickedCallbacks.add(cb)
    }

    fun setText(text: String) {
        textArea.text = text
        showBottomPanel()
    }

    fun getText(): String = textArea.text
}