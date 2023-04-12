package sportisimo.utils

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.treeStructure.Tree
import sportisimo.data.azure.PullRequestIterationChangeData
import sportisimo.data.renderers.FileCellData
import sportisimo.data.renderers.PathToNodeData
import sportisimo.renderers.tree.FileCellRenderer
import java.awt.event.MouseEvent
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode

object FileTreeUtils
{
    fun getFileTree(
        rootNode: DefaultMutableTreeNode,
        onMouseEvent: ((MouseEvent?, FileCellData) -> Unit)? = null
    ): Tree
    {
        val tree =  Tree(rootNode).apply {
            cellRenderer = FileCellRenderer()

            addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents {
                if(it == null || onMouseEvent == null) return@onAnyMouseEvents

                val paths = this.getPathForLocation(it.x, it.y) ?: return@onAnyMouseEvents

                val lastPath = paths.lastPathComponent
                if(lastPath !is DefaultMutableTreeNode) return@onAnyMouseEvents

                val data = lastPath.userObject
                if(data !is FileCellData) return@onAnyMouseEvents

                onMouseEvent.invoke(it, data)
            })
        }

        expandAllTreeNodes(tree)

        return tree
    }

    fun prepareChangedFilesNodes(project: Project, changedFilesData: List<PullRequestIterationChangeData>): DefaultMutableTreeNode
    {
        val rootFolderName = project.basePath?.split("/")?.lastOrNull() ?: ""
        val rootData = FileCellData(AllIcons.Nodes.IdeaProject, rootFolderName, project.basePath!!, true)
        val rootNode = DefaultMutableTreeNode(rootData)

        val pathToNodes = mutableListOf<PathToNodeData>()
        changedFilesData.forEach {
            prepareFolderStructure(project, it.item.path ?: it.originalPath!!, pathToNodes)
        }
        changedFilesData.forEach {
            prepareFilesStructure(project, it.item.path ?: it.originalPath!!, pathToNodes, it)
        }

        mergeFolderStructureIfPossible(pathToNodes.first(), pathToNodes)

        pathToNodes.forEach { pathToNode ->
            if(pathToNode.parentPath == "")
            {
                rootNode.add(pathToNode.node)
                return@forEach
            }

            val parentNode = pathToNodes.find { pathToNode.parentPath == it.path } ?: return@forEach
            parentNode.node.add(pathToNode.node)
        }

        return rootNode
    }

    private fun expandAllTreeNodes(tree: JTree)
    {
        var j = tree.rowCount
        var i = 0
        while (i < j)
        {
            tree.expandRow(i)
            i += 1
            j = tree.rowCount
        }
    }

    private fun prepareFileNode(fileName: String, currentPath: String, changedFileData: PullRequestIterationChangeData): DefaultMutableTreeNode
    {
        val fileType = FileUtils.getFileTypeByFileName(fileName)
        val icon = fileType.icon
        val presentation = FileCellData(icon, fileName, currentPath, false, changedFileData)
        return DefaultMutableTreeNode(presentation)
    }

    private fun prepareFolderNode(folderName: String, currentPath: String): DefaultMutableTreeNode
    {
        val icon = AllIcons.Nodes.Folder
        val presentation = FileCellData(icon, folderName, currentPath, true)
        return DefaultMutableTreeNode(presentation)
    }

    private fun prepareFolderStructure(
        project: Project,
        path: String,
        pathToNode: MutableList<PathToNodeData>
    )
    {
        val pathParts = path.split("/")
        if(pathParts.isEmpty()) return

        var currentPath = ""
        pathParts.forEach { part ->
            if(part == "") return@forEach

            val parentPath = currentPath

            currentPath += "/$part"
            if(pathToNode.any { it.path == currentPath }) return@forEach

            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}/$currentPath") ?: return@forEach
            if(!virtualFile.isDirectory) return@forEach

            val node = prepareFolderNode(part, currentPath)
            pathToNode.add(PathToNodeData(currentPath, true, parentPath, node))
        }
    }

    private fun prepareFilesStructure(
        project: Project,
        path: String,
        pathToNode: MutableList<PathToNodeData>,
        changedFileData: PullRequestIterationChangeData
    )
    {
        val pathParts = path.split("/")
        if(pathParts.isEmpty()) return

        var currentPath = ""
        pathParts.forEach { part ->
            if(part == "") return@forEach

            val parentPath = currentPath

            currentPath += "/$part"
            if(pathToNode.any { it.path == currentPath }) return@forEach

            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}/$currentPath")

            val node: DefaultMutableTreeNode = if(virtualFile == null)
            {
                prepareFileNode(part, currentPath, changedFileData)
            }
            else
            {
                if(virtualFile.isDirectory) prepareFolderNode(part, currentPath) else prepareFileNode(part, currentPath, changedFileData)
            }

            pathToNode.add(PathToNodeData(currentPath, false, parentPath, node))
        }
    }

    private fun mergeFolderStructureIfPossible(pathToNode: PathToNodeData, pathToNodes: MutableList<PathToNodeData>)
    {
        val children = pathToNodes.filter { it.parentPath == pathToNode.path }
        if(children.isEmpty()) return

        val containsFile = children.any { !it.isFolder }
        if(containsFile)
        {
            children.forEach {
                if(!it.isFolder) return@forEach
                mergeFolderStructureIfPossible(it, pathToNodes)
            }
            return
        }

        val uniqueChildPaths = mutableListOf<String>()
        children.forEach {
            if(!uniqueChildPaths.contains(it.path)) uniqueChildPaths.add(it.path)
        }

        val newNodes = mutableListOf<PathToNodeData>()
        uniqueChildPaths.forEach {
            val cleanPath = it.replace(pathToNode.parentPath, "").replaceFirst("/", "")
            val folderNode = prepareFolderNode(cleanPath, it)
            val data = PathToNodeData(it, true, pathToNode.parentPath, folderNode)
            newNodes.add(data)
        }

        newNodes.forEach { newNode ->
            val nodeChildren = pathToNodes.filter { it.parentPath == newNode.path }

            nodeChildren.forEach {
                it.parentPath = newNode.path
            }
        }

        val indexOfParent = pathToNodes.indexOf(pathToNode)
        pathToNodes.remove(pathToNode)

        pathToNodes.addAll(indexOfParent, newNodes)

        newNodes.forEach {
            if(!it.isFolder) return@forEach
            mergeFolderStructureIfPossible(it, pathToNodes)
        }
    }
}