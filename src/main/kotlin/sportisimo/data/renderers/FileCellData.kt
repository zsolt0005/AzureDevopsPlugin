package sportisimo.data.renderers

import sportisimo.data.azure.PullRequestIterationChangeData
import javax.swing.Icon

data class FileCellData(
    val icon: Icon,
    val name: String,
    val path: String,
    val isFolder: Boolean,
    val changedFileData: PullRequestIterationChangeData? = null
)