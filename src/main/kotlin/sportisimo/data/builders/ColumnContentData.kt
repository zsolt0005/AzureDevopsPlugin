package sportisimo.data.builders

import sportisimo.ui.builders.PanelBuilder
import java.awt.Component

data class ColumnContentData(
    val component: Component? = null,
    val builder: PanelBuilder? = null
)