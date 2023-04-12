package sportisimo.ui.tabs.toolwindow

import java.awt.Color
import javax.swing.JPanel

interface ITab
{
    fun getPanel(): JPanel
    fun getName(): String
    fun getDescription(): String
    fun getColor(): Color?
}