package sportisimo.renderers.tree

import com.intellij.ide.util.treeView.NodeRenderer
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.ui.tree.TreeUtil
import sportisimo.data.renderers.FileCellData
import javax.swing.JTree

// ProjectViewRenderer
class FileCellRenderer: NodeRenderer()
{
    init {
        isOpaque = false
        isIconOpaque = false
        isTransparentIconBackground = true
    }

    override fun customizeCellRenderer(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean)
    {
        val data = TreeUtil.getUserObject(value)
        if(data !is FileCellData) return

        icon = fixIconIfNeeded(data.icon, selected, hasFocus)
        append(data.name)
        toolTipText = null

        if(!data.isFolder)
        {
            append(" [${data.changedFileData?.changeType!!}] ", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES)
        }
    }
}