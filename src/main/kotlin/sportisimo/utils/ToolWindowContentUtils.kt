package sportisimo.utils

import com.intellij.openapi.wm.ToolWindow
import sportisimo.ui.builders.PanelBuilder
import sportisimo.ui.tabs.toolwindow.ITab
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Utils to work with ToolPanel contents.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object ToolWindowContentUtils
{
    /**
     * Creates a loading window for the given ToolWindow.
     *
     * @param toolWindow The tool window to apply the new content to.
     */
    fun createLoadingWindow(toolWindow: ToolWindow)
    {
        val panel = UIUtils.createPanelWithFlowLayout()
        panel.add(PanelBuilder { row { column { loading() } } }.build())

        applyNewToolWindowContent(toolWindow, panel)
    }

    /**
     * Creates an error window for the given ToolWindow.
     *
     * @param toolWindow The tool window to apply the new content to.
     * @param htmlMessage Content of the window.
     */
    fun createErrorWindow(toolWindow: ToolWindow, htmlMessage: String)
    {
        val panel = UIUtils.createPanelWithFlowLayout()

        val contentPanel = com.intellij.ui.dsl.builder.panel { row { text(htmlMessage) } }
        panel.add(contentPanel)

        applyNewToolWindowContent(toolWindow, panel)
    }

    /**
     * Applies new content WITHOUT tabs to a ToolWindow.
     *
     * @param toolWindow The tool window to apply the new content to.
     * @param panel The panel that will be applied to the ToolWindow.
     */
    private fun applyNewToolWindowContent(toolWindow: ToolWindow, panel: JPanel)
    {
        SwingUtilities.invokeLater {
            // Display name null will turn off tabbed version of the tool window.
            val content = toolWindow.contentManager.factory.createContent(panel, null, true)
            toolWindow.contentManager.removeAllContents(true)
            toolWindow.contentManager.addContent(content)
        }
    }

    /**
     * Applies new content WITH tabs to a ToolWindow.
     *
     * @param toolWindow The tool window to apply the new content to.
     * @param tabs The tabs that will be applied to the ToolWindow.
     */
    fun applyNewToolWindowContentWithTabs(toolWindow: ToolWindow, tabs: List<ITab>)
    {
        SwingUtilities.invokeLater {
            toolWindow.contentManager.removeAllContents(true)

            tabs.forEach {
                val content = toolWindow.contentManager.factory.createContent(it.getPanel(), it.getName(), true).apply {
                    description = it.getDescription()
                    tabColor = it.getColor()
                }
                toolWindow.contentManager.addContent(content)
            }
        }
    }
}