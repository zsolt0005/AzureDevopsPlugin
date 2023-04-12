package sportisimo.ui.elements.adapters

import sportisimo.ui.builders.PanelBuilder
import javax.swing.JPanel

class ListPanelStringAdapter: AListPanelAdapter()
{
    override fun <T>createLine(value: T): JPanel
    {
        value as String

        return PanelBuilder {
            row { column { label(value) } }
        }.build()
    }

    override fun rowHeight(): Int
    {
        return PanelBuilder { row { column { label(" ") } } }.build().preferredSize.height
    }
}