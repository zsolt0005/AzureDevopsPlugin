package sportisimo.ui.builders

import com.intellij.openapi.ui.OnePixelDivider
import com.intellij.ui.SeparatorComponent
import com.intellij.ui.TitledSeparator
import com.intellij.util.ui.JBUI
import sportisimo.data.builders.ColumnAlignment
import sportisimo.data.builders.ColumnContentData
import sportisimo.data.builders.ColumnData
import sportisimo.data.builders.RowData
import java.awt.*
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.Border

class PanelBuilder(builderCallback: PanelBuilder.() -> Unit)
{
    private var panel: JPanel = JPanel().apply { layout = GridBagLayout() }

    private val rows: MutableList<RowData> = mutableListOf()

    private var alignColumnStarts = false

    init { this.builderCallback() }

    fun preferredSize(width: Int, height: Int)
    {
        panel.preferredSize = Dimension(width, height)
    }

    fun border(border: Border)
    {
        panel.border = border
    }

    fun background(color: Color)
    {
        panel.background = color
    }

    fun alignColumnStarts()
    {
        alignColumnStarts = true
    }

    fun row(builder: RowBuilder.() -> Unit): JPanel
    {
        val rowPanel = getRowPanel()

        val rowBuilder = RowBuilder(rowPanel)
        rowBuilder.builder()

        verticalGap(2) // TODO Hardcoded value
        rows.add(rowBuilder.rowData)
        verticalGap(2) // TODO Hardcoded value

        return rowPanel
    }

    fun emptyRow()
    {
        val columnContent = ColumnContentData(
            JLabel(" ").apply {
                preferredSize = Dimension(1, preferredSize.height)
            }
        )

        val contents = listOf(columnContent)
        val rowData = RowData(
            getRowPanel()
        ).apply { columns.add(ColumnData(getColumnPanel(), contents, ColumnAlignment.Left)) }

        rows.add(rowData)
    }

    fun rowFiller()
    {
        val columnContent = ColumnContentData(Box.createVerticalGlue())

        val contents = listOf(columnContent)
        val rowData = RowData(
            getRowPanel()
        ).apply { columns.add(ColumnData(getColumnPanel(), contents, ColumnAlignment.Left)) }

        rows.add(rowData)
    }

    fun verticalGap(size: Int)
    {
        val contents = listOf(ColumnContentData(Box.createVerticalStrut(size)))
        val columnData = ColumnData(getColumnPanel(), contents, ColumnAlignment.Left)
        val rowData = RowData(
            getRowPanel()
        ).apply { columns.add(columnData) }
        rows.add(rowData)
    }

    fun separator(title: String? = null, marginTop: Int = 0, marginBottom: Int = 0)
    {
        verticalGap(marginTop)

        val separator = if (title == null)
        {
            SeparatorComponent(0, OnePixelDivider.BACKGROUND, null)
        }
        else
        {
            TitledSeparator(title)
        }

        separator.border = null

        val contents = listOf(ColumnContentData(separator))
        val columnData = ColumnData(getColumnPanel(), contents, ColumnAlignment.Left)
        val rowData = RowData(
            getRowPanel()
        ).apply { columns.add(columnData) }
        rows.add(rowData)

        verticalGap(marginBottom)
    }

    fun separator(title: String? = null, margin: Int = 0) = separator(title, margin, margin)

    fun indent(builder: PanelBuilder.() -> Unit)
    {
        row {
            column {
                horizontalGap(24) // TODO Hardcoded value
                panel(builder = builder)
            }
        }
    }

    fun group(title: String, indent: Boolean, builder: PanelBuilder.() -> Unit)
    {
        separator(title, 24, 12) // TODO Hardcoded value
        row {
            column {
                if(indent) horizontalGap(24) // TODO Hardcoded value
                panel(builder = builder)
            }
        }
    }

    fun group(builder: PanelBuilder.() -> Unit) = group("", false, builder)

    fun group(indent: Boolean, builder: PanelBuilder.() -> Unit) = group("", indent, builder)

    fun group(title: String, builder: PanelBuilder.() -> Unit) = group(title, false, builder)

    fun build(): JPanel
    {
        val panelConstraints = getGridBagConstraints().apply {
            fill = GridBagConstraints.HORIZONTAL
            insets = JBUI.emptyInsets()
            weighty = 0.0
        }

        rows.forEach { row ->
            val rowPanel = row.panel

            row.columns.forEach { column ->
                val columnPanel = column.panel

                if(column.alignment == ColumnAlignment.Right || column.alignment == ColumnAlignment.Center)
                {
                    columnPanel.add(Box.createHorizontalGlue())
                }

                column.content.forEach { content ->
                    if(content.component != null) columnPanel.add(content.component)
                    else if(content.builder != null)
                    {
                        columnPanel.add(content.builder.build())
                    }
                }

                if(column.alignment == ColumnAlignment.Center)
                {
                    columnPanel.add(Box.createHorizontalGlue())
                }

                rowPanel.add(columnPanel)
            }

            panelConstraints.gridy++
            panel.add(rowPanel, panelConstraints)
        }

        val lastRowPanel = JPanel().apply {
            layout = BorderLayout()
        }

        panelConstraints.apply {
            fill = GridBagConstraints.BOTH
            gridy++
            weighty = 10.0
        }

        panel.add(lastRowPanel, panelConstraints)

        if(alignColumnStarts) setAlignments()

        return panel
    }

    private fun setAlignments()
    {
        val columnIndexToWidth = getMaxWidthColumn()

        setColumnWidths(columnIndexToWidth)
    }

    private fun getMaxWidthColumn(): MutableMap<Int, Int>
    {
        val columnIndexToWidth = mutableMapOf<Int, Int>()

        rows.forEach { row ->
            row.columns.forEachIndexed { index, column ->
                if(!columnIndexToWidth.keys.contains(index)) columnIndexToWidth[index] = 0

                val lastBiggest = columnIndexToWidth[index]!!
                if(column.panel.preferredSize.width > lastBiggest)
                {
                    columnIndexToWidth[index] = column.panel.preferredSize.width
                }
            }
        }

        return columnIndexToWidth
    }

    private fun setColumnWidths(columnIndexToWidth: MutableMap<Int, Int>)
    {
        rows.forEach { row ->
            row.columns.forEachIndexed { index, column ->
                val biggestColumn = columnIndexToWidth[index]
                column.panel.minimumSize = Dimension(biggestColumn!!, column.panel.minimumSize.height)
                column.panel.maximumSize = Dimension(biggestColumn, column.panel.maximumSize.height)
            }
        }
    }

    private fun getGridBagConstraints(): GridBagConstraints
    {
        return GridBagConstraints().apply {
            weightx = 1.0
            weighty = 1.0

            gridx = 0
            gridy = 0
            gridwidth = 1
        }
    }

    companion object
    {
        fun getColumnPanel() = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }

        fun getRowPanel() = JPanel().apply { layout = BoxLayout(this, BoxLayout.LINE_AXIS) }
    }
}