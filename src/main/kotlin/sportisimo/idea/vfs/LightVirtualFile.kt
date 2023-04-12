package sportisimo.idea.vfs

import com.intellij.testFramework.LightVirtualFile

class LightVirtualFile(
    name: String,
    content: CharSequence,
    val filePath: String
): LightVirtualFile(name, content)