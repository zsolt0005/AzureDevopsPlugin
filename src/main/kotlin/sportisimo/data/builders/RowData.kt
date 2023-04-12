package sportisimo.data.builders

import javax.swing.JPanel

data class RowData(
    val panel: JPanel,
    val columns: MutableList<ColumnData> = mutableListOf()
)