package sportisimo.ui.builders

import sportisimo.data.builders.ColumnAlignment
import sportisimo.data.builders.ColumnData
import sportisimo.data.builders.RowData
import javax.swing.JPanel

class RowBuilder(val rowPanel: JPanel)
{
    val rowData: RowData = RowData(rowPanel)

    fun column(
        alignment: ColumnAlignment = ColumnAlignment.Left,
        builder: ColumnBuilder.() -> Unit
    ): JPanel
    {
        val columnBuilder = ColumnBuilder()
        columnBuilder.builder()
        columnBuilder.horizontalGap(8) // TODO Hardcoded value

        val panel = PanelBuilder.getColumnPanel()

        rowData.columns.add(ColumnData(panel, columnBuilder.contentData, alignment))

        return panel
    }
}