package sportisimo.azure.clients

import com.google.gson.Gson
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.ProjectData
import sportisimo.data.azure.WorkItemSearchResultData
import sportisimo.data.azure.client.WorkItemSearchData
import sportisimo.data.azure.responses.WorkItemSearchResultResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import java.io.IOException

class SearchClient(private val connection: Connection)
{
    /**
     * Search work items.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun searchWorkItems(project: ProjectData, searchData: WorkItemSearchData): List<WorkItemSearchResultData>
    {
        val url = getBaseUrl(project.id) + "search/workItemSearchResults"

        val requestBody = Gson().toJson(searchData)
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, requestBody)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.WorkItems)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, WorkItemSearchResultResponseData::class.java)

        return responseObject.results ?: listOf()
    }

    private fun getBaseUrl(project: String? = null): String
    {
        var baseUrl = "https://almsearch.dev.azure.com/${connection.organization}/"

        if(project != null)
        {
            baseUrl += "$project/"
        }

        baseUrl += "_apis/"

        return baseUrl
    }
}