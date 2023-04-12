package sportisimo.ui.elements.adapters

import sportisimo.data.azure.WorkItemSearchResultData
import sportisimo.data.azure.WorkItemSearchResultFieldsData
import sportisimo.data.builders.ColumnAlignment
import sportisimo.data.ui.ActionData
import sportisimo.ui.builders.PanelBuilder
import sportisimo.utils.DateTimeUtils
import sportisimo.utils.EventUtils
import sportisimo.utils.ImageUtils
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class WorkItemSearchResultAdapter(
    private val onMouseClicked: ((e: MouseEvent?) -> Unit)? = null,
    private val action: ActionData? = null,
): AListPanelAdapter()
{
    override fun <T> createLine(value: T): JPanel
    {
        value as WorkItemSearchResultData
        val fields = value.fields

        var actionIcon: JLabel? = null

        val panel = PanelBuilder {
            verticalGap(10)
            row {
                column {
                    icon(ImageUtils.svgToIcon(value.svgIcon, 16, 16)); horizontalGap(10)
                    boldLabel("${fields.workItemType} ${fields.id}: ${fields.title}")
                    horizontalGap(15)
                }
                if(action != null)
                {
                    column(ColumnAlignment.Right) {
                        actionIcon = icon(action.icon)
                    }
                }
            }
            row {
                column {
                    horizontalGap(24)
                    runCatching {
                        comment(DateTimeUtils.format(fields.createdDate))
                    }
                        .onFailure {
                            comment(fields.createdDate)
                        }

                    horizontalGap(12)
                    boldComment(fields.assignedTo)
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
            onMouseClicked = { ev -> onMouseClicked?.let { it(ev) } }
        ))

        return panel
    }

    override fun rowHeight(): Int
    {
        val sampleItem = WorkItemSearchResultData(
            WorkItemSearchResultFieldsData(" ", " ", " ", " ", " ", " ", " ", " ", " "),
            ""
        )

        return createLine(sampleItem).preferredSize.height
    }
}