package sportisimo.ui.elements

import com.intellij.ui.components.JBScrollPane
import sportisimo.ui.elements.adapters.AListPanelAdapter
import sportisimo.utils.MathUtils
import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.JScrollPane

class ListPanel<T>(private val list: List<T>, private val rows: Int, private val adapter: AListPanelAdapter) : JPanel()
{
    private val internalPanel: JPanel = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder()
    }

    private val internalScrollPane: JScrollPane = JBScrollPane(
        internalPanel, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    ).apply {
        border = BorderFactory.createEmptyBorder()
    }

    private var itemsToPanelsMap: MutableList<Pair<T, JPanel>> = mutableListOf()
    private var rowHeight: Int = 0

    init {
        layout = BoxLayout(this, BoxLayout.PAGE_AXIS)
        border = BorderFactory.createEmptyBorder(0, 5, 0, 0) // TODO Hard coded value

        rowHeight = adapter.rowHeight()

        add(internalScrollPane)

        updateUi()
        drawList()
    }

    private fun drawList()
    {
        list.forEach { addItem(it) }
    }

    fun addItem(item: T)
    {
        val linePanel = adapter.createLine(item)
        itemsToPanelsMap.add(Pair(item, linePanel))

        internalPanel.add(linePanel)
        updateUi()
    }

    fun removeItem(item: T)
    {
        val itemToPanelMap = itemsToPanelsMap.find {
            it.first == item
        } ?: return

        itemsToPanelsMap.remove(itemToPanelMap)

        internalPanel.remove(itemToPanelMap.second)
        updateUi()
    }

    fun removeAllItems()
    {
        itemsToPanelsMap.forEach { internalPanel.remove(it.second) }
        itemsToPanelsMap.clear()
        updateUi()
    }

    private fun updateUi()
    {
        val maxHeight = rowHeight * rows
        val currentHeight = rowHeight * itemsToPanelsMap.size
        val preferredHeight = MathUtils.clamp(currentHeight, 0, maxHeight)

        minimumSize = Dimension(minimumSize.width, preferredHeight)
        maximumSize = Dimension(maximumSize.width, preferredHeight)
        preferredSize = Dimension(preferredSize.width, preferredHeight)

        internalPanel.revalidate()
        internalPanel.repaint()

        revalidate()
        repaint()
    }

    fun getItems() = itemsToPanelsMap.map { it.first }

    fun getItem(index: Int) = itemsToPanelsMap[index].first

    fun getItem(panel: JPanel) = itemsToPanelsMap.find { it.second == panel }?.first
}