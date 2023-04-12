package sportisimo.renderers.combobox

import sportisimo.data.ActionButtonData
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.JList
import javax.swing.plaf.basic.BasicComboBoxRenderer

class ActionButtonComboBoxRenderer: BasicComboBoxRenderer()
{
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component
    {
        value as ActionButtonData

        super.getListCellRendererComponent(list, value.title, index, isSelected, cellHasFocus)
        icon = value.icon
        border = BorderFactory.createEmptyBorder(10, 0, 10, 0)

        return this
    }
}