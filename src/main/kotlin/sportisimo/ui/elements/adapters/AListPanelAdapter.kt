package sportisimo.ui.elements.adapters

import javax.swing.JPanel

abstract class AListPanelAdapter
{
    abstract fun <T>createLine(value: T): JPanel

    abstract fun rowHeight(): Int
}