package sportisimo.azure

import sportisimo.data.ConnectionData
import sportisimo.data.azure.RepositoryData
import sportisimo.data.azure.RepositoryRefData
import sportisimo.exceptions.NotFoundException
import sportisimo.states.AppSettingsState
import sportisimo.states.ProjectDataState

object DevOpsHelper
{
    private val settings = AppSettingsState.getInstance()

    fun detectDevOpsConnectionFromRepositoryName(repositoryName: String): List<ConnectionData>
    {
        val possibleConnections: MutableList<ConnectionData> = mutableListOf()

        settings.azureConnections.connections.forEach { connectionData ->
            runCatching {
                val connection = Connection(connectionData.organization, connectionData.token)
                val repositories: List<RepositoryData> = connection.gitClient.getRepositories(connectionData.project)

                repositories.forEach { repositoryData ->
                    if(repositoryData.name == repositoryName)
                    {
                        possibleConnections.add(connectionData)
                    }
                }
            }
        }

        return possibleConnections
    }

    /**
     * @param repositoryName
     * @throws NotFoundException
     */
    fun findRepositoryByRepositoryName(connectionData: ConnectionData, repositoryName: String): RepositoryData
    {
        runCatching {
            val connection = Connection(connectionData.organization, connectionData.token)
            val repositories = connection.gitClient.getRepositories(connectionData.project)

            repositories.forEach { repositoryData ->
                if(repositoryData.name == repositoryName)
                {
                    return repositoryData
                }
            }
        }

        throw NotFoundException("No repository was found")
    }

    /**
     * @param connectionData
     * @param repository
     * @param branchName
     * @throws NotFoundException
     */
    fun getBranchFromRepositoryAndBranchName(connectionData: ConnectionData, repository: RepositoryData, branchName: String): RepositoryRefData
    {
        runCatching {
            val connection = Connection(connectionData.organization, connectionData.token)
            val branches = connection.gitClient.getBranches(repository) // Contains tags too

            branches.forEach {
                if(it.name == "refs/heads/$branchName")
                {
                    return it
                }
            }
        }

        throw NotFoundException("Branch <b>$branchName</b> was not found on the remote.<br><center><b>Did you push your branch?</b></center>")
    }

    /**
     * Checks if the saved connection still exists in the connections list.
     *
     * @param cacheData
     * @return true if still exists, false otherwise.
     */
    fun connectionStillExists(cacheData: ProjectDataState): Boolean
    {
        return settings.azureConnections.connections.any {
            cacheData.connectionData != null
         && it.organization == cacheData.connectionData?.organization
         && it.project.id == cacheData.connectionData?.project?.id
        }
    }
}