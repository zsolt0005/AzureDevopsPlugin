package sportisimo.data

import java.util.concurrent.Callable
import javax.swing.Icon

data class ActionButtonData(
    val title: String,
    val icon: Icon,
    val callback: Callable<*>
)