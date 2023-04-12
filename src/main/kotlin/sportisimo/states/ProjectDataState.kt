package sportisimo.states

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import sportisimo.data.*
import sportisimo.data.azure.IdentityData
import sportisimo.data.azure.RepositoryData
import sportisimo.data.azure.RepositoryRefData
import sportisimo.data.converters.DataConverters

@State(name = "com.sportisimo.devops.DevOpsToolWindow", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ProjectDataState: PersistentStateComponent<ProjectDataState>
{
    @OptionTag(converter = DataConverters.IdentityDataConverter::class)
    var currentUser: IdentityData? = null

    @OptionTag(converter = DataConverters.ConnectionDataConverter::class)
    var connectionData: ConnectionData? = null

    @OptionTag(converter = DataConverters.RepositoryDataConverter::class)
    var repositoryData: RepositoryData? = null

    @OptionTag(converter = DataConverters.RepositoryRefDataConverter::class)
    var branchData: RepositoryRefData? = null

    @OptionTag(converter = DataConverters.GitRepositoryDataConverter::class)
    var gitData: GitRepositoryData? = null

    @OptionTag(converter = DataConverters.WorkItemTypesDataConverter::class)
    var workItemTypes: WorkItemTypesData? = null

    @OptionTag(converter = DataConverters.PullRequestsDataConverter::class)
    var pullRequests: PullRequestsData? = null

    @OptionTag(converter = DataConverters.ProjectTeamsDataConverter::class)
    var projectTeams: ProjectTeamsData? = null

    @Transient var lastOpenedPullRequest: LastOpenedPullRequestData? = null

    var selectedManually: Boolean = false

    fun clearData()
    {
        currentUser = null
        connectionData = null
        repositoryData = null
        branchData = null
        gitData = null
        workItemTypes = null
        pullRequests = null
        projectTeams = null
        lastOpenedPullRequest = null
        selectedManually = false
    }
    override fun getState() = this

    override fun loadState(state: ProjectDataState) = XmlSerializerUtil.copyBean(state, this)

    companion object
    {
        fun getInstance(project: Project): ProjectDataState
        {
            return project.getService(ProjectDataState::class.java)
        }
    }
}