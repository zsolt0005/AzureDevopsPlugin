package sportisimo.data.ui

import java.awt.event.MouseEvent
import javax.swing.Icon
import javax.swing.JPanel

data class ActionData(
    val icon: Icon,
    val mouseClickEvent: (e: MouseEvent?, panel: JPanel) -> Unit
)