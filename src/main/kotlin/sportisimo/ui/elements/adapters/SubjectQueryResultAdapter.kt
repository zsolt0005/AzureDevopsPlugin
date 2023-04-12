package sportisimo.ui.elements.adapters

import sportisimo.data.azure.SubjectQueryResultData
import sportisimo.data.builders.ColumnAlignment
import sportisimo.data.ui.ActionData
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.EventUtils
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class SubjectQueryResultAdapter(
    private val showCheckBox: Boolean,
    private val onMouseClicked: ((e: MouseEvent?) -> Unit)? = null,
    private val action: ActionData? = null,
): AListPanelAdapter()
{
    override fun <T> createLine(value: T): JPanel
    {
        value as SubjectQueryResultData

        var actionIcon: JLabel? = null

        val panel = PanelBuilder {
            verticalGap(10)
            row {
                column {
                    asyncIcon(value.avatar!!); horizontalGap(10)
                    boldLabel(value.displayName)
                }

                if(showCheckBox)
                {
                    column {
                        val requiredCheckBox = checkBox("Required")

                        requiredCheckBox.addActionListener {
                            value.isRequired = requiredCheckBox.isSelected
                        }
                    }
                }

                if(action != null)
                {
                    column(ColumnAlignment.Right) {
                        actionIcon = icon(action.icon)
                    }
                }
            }
        }.build()

        actionIcon?.addMouseListener(object: MouseAdapter(){
            override fun mouseClicked(e: MouseEvent?)
            {
                action!!.mouseClickEvent(e, panel)
            }
        })

        panel.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
            onMouseClicked = { ev ->
                onMouseClicked?.let { it(ev) }
            }
        ))

        return panel
    }

    override fun rowHeight(): Int
    {
        return 42 // TODO hardcoded value
    }
}