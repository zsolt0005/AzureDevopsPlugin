package sportisimo.azure.clients

import com.google.gson.Gson
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.ProjectData
import sportisimo.data.azure.ProjectTeamData
import sportisimo.data.azure.responses.ProjectTeamsResponseData
import sportisimo.data.azure.responses.ProjectsResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import java.io.IOException

class CoreClient(private val connection: Connection)
{
    /**
     * Get all the projects of the organization.
     *
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getProjects(): List<ProjectData>
    {
        val url = getBaseUrl() + "projects"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.Projects)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, ProjectsResponseData::class.java)

        return responseObject.value
    }

    /**
     * Get all teams within the project.
     *
     * @throws IOException if the request could not be executed due to cancellation, a connectivity problem or timeout.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getProjectTeams(project: ProjectData): List<ProjectTeamData>
    {
        val url = getBaseUrl() + "projects/${project.id}/teams"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.Teams)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, ProjectTeamsResponseData::class.java)

        return responseObject.value
    }

    private fun getBaseUrl(project: String? = null): String
    {
        var baseUrl = "https://dev.azure.com/${connection.organization}/"

        if(project != null)
        {
            baseUrl += "$project/"
        }

        baseUrl += "_apis/"

        return baseUrl
    }
}