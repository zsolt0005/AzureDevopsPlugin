package sportisimo.azure

import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import git4idea.branch.GitBranchUtil
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import sportisimo.exceptions.GitException
import sportisimo.states.AppSettingsState
import sportisimo.utils.NotificationUtils

object GitHelper
{
    private val settings = AppSettingsState.getInstance()

    /**
     * @param project
     * @return Current repository name
     * @throws GitException
     */
    fun getRepositoryName(project: Project): String
    {
        val repository = GitBranchUtil.getCurrentRepository(project) ?: throw GitException("No repository detected")

        val folderName = repository.root.name
        val devOpsRemotes = getDevOpsRemotesFromRepository(repository)
        val devOpsRemoteNames = devOpsRemotes.mapNotNull { it.firstUrl?.split("/")?.last() }

        // No remotes (This should never happen)
        if(devOpsRemoteNames.isEmpty())
        {
            NotificationUtils.notify(
                "Repository remote not found",
                "No Azure DevOps remotes found for your repository",
                NotificationType.WARNING,
                project = project,
                isAllowed = settings.notificationStates.localGit
            )
            return folderName
        }

        // One remote found, prefer the remote value
        if(devOpsRemoteNames.size == 1)
        {
            val devOpsRepoName = devOpsRemoteNames.first()
            if(folderName != devOpsRepoName)
            {
                NotificationUtils.notify(
                    "Repository name mismatch",
                    "Your folder name is: $folderName but the remote is $devOpsRepoName",
                    NotificationType.WARNING,
                    project = project,
                    isAllowed = settings.notificationStates.localGit
                )
            }

            return devOpsRepoName
        }

        // Multiple remotes found, return the one that is present more times
        val repoToCount: MutableMap<String, Int> = mutableMapOf(folderName to 1) // Directory name as initial value
        devOpsRemoteNames.forEach {
            if(repoToCount[it] == null) repoToCount[it] = 1
            else repoToCount[it] = repoToCount[it]!! + 1
        }

        return repoToCount.maxBy { it.value }.key
    }

    /**
     * @param project
     * @return Current branch name
     * @throws GitException
     */
    fun getBranchName(project: Project): String
    {
        val repository = GitBranchUtil.getCurrentRepository(project) ?: throw GitException("No repository detected")
        return repository.currentBranchName ?: throw GitException("Branch name not found")
    }

    /**
     * @param project
     * @return true if the repository is hosted with Azure DevOps
     */
    fun isRemoteDevOps(project: Project): Boolean
    {
        val gitRepository = GitBranchUtil.getCurrentRepository(project) ?: return false
        return getDevOpsRemotesFromRepository(gitRepository).isNotEmpty()
    }

    private fun getDevOpsRemotesFromRepository(gitRepository: GitRepository): List<GitRemote>
    {
        return gitRepository.remotes.filter {
            it.firstUrl != null
         && (
                it.firstUrl!!.contains("dev.azure.com")
             || it.firstUrl!!.contains("visualstudio.com")
            )
        }
    }
}