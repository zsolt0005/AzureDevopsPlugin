package sportisimo.data.renderers

import javax.swing.tree.DefaultMutableTreeNode

data class PathToNodeData(
    var path: String,
    val isFolder: Boolean,
    var parentPath: String,
    val node: DefaultMutableTreeNode
)
{
    override fun toString(): String
    {
        return "$path -> $parentPath"
    }
}