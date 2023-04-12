package sportisimo.data.builders

import javax.swing.JPanel

data class ColumnData(
    val panel: JPanel,
    val content: List<ColumnContentData>,
    val alignment: ColumnAlignment
)

enum class ColumnAlignment(val value: Int)
{
    Left(0),
    Center(1),
    Right(2),
}