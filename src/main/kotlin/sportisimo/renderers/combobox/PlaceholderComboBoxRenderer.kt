package sportisimo.renderers.combobox

import java.awt.Component
import javax.swing.JList
import javax.swing.plaf.basic.BasicComboBoxRenderer

/**
 * A combo box renderer that can display a placeholder text, if the value is empty.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 *
 * @property placeholder The placeholder text to be shown.
 */
class PlaceholderComboBoxRenderer(private val placeholder: String): BasicComboBoxRenderer()
{
    override fun getListCellRendererComponent(
        list: JList<*>?,
        value: Any?,
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component
    {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

        if (value == null){
            text = placeholder
        }

        return this
    }
}