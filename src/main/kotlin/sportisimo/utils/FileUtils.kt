package sportisimo.utils

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import sportisimo.idea.vfs.LightVirtualFile

object FileUtils
{
    fun getFileTypeByFileName(name: String): FileType
    {
        val fileTypeManager = FileTypeManager.getInstance()
        val fileType = fileTypeManager.getFileTypeByFileName(name)
        if (fileType.displayName != "Unknown") return fileType

        return fileTypeManager.getFileTypeByFileName("test.txt")
    }

    fun getFileByEditor(editor: Editor): VirtualFile?
    {
        return FileDocumentManager.getInstance().getFile(editor.document)
    }

    fun getFilePathFromFile(file: VirtualFile, project: Project): String =
        if(file is LightVirtualFile) file.filePath else getRelativePathFromAbsolutePath(file.path, project)

    fun isLeftSideFile(file: VirtualFile): Boolean = file is LightVirtualFile

    fun getRelativePathFromAbsolutePath(path: String, project: Project) = path
        .replace("file://", "")
        .replace(project.basePath!!, "")

    fun getFileNameFromPath(path: String) = path.split("/").last()
}