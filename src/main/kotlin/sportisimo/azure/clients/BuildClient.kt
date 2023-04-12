package sportisimo.azure.clients

import com.google.gson.Gson
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.BuildRunData
import sportisimo.data.azure.ProjectData
import sportisimo.data.azure.client.BuildRunsSearchData
import sportisimo.data.azure.responses.BuildRunsResponseData
import sportisimo.exceptions.InvalidArgumentException

class BuildClient(private val connection: Connection)
{
    fun getRuns(project: ProjectData, searchData: BuildRunsSearchData): List<BuildRunData>
    {
        val params = searchData.toString()
        if(params.isEmpty())
        {
            throw InvalidArgumentException("Search data needs at least one search criteria to be set!")
        }

        val url = getBaseUrl(project.id) + "build/builds?${searchData}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.BuildRuns)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, BuildRunsResponseData::class.java)

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