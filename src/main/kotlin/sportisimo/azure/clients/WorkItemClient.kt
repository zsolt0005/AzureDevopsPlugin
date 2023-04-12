package sportisimo.azure.clients

import com.google.gson.Gson
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.ProjectData
import sportisimo.data.azure.WorkItemData
import sportisimo.data.azure.WorkItemTypeData
import sportisimo.data.azure.responses.WorkItemTypesResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import java.io.IOException

class WorkItemClient(private val connection: Connection)
{
    /**
     * Get work item types.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getWorkItemTypes(project: ProjectData): List<WorkItemTypeData>
    {
        val url = getBaseUrl(project.id) + "wit/workItemTypes/"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.WorkItems)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, WorkItemTypesResponseData::class.java)

        val commonClient = connection.commonClient

        responseObject.value.forEach {
            runCatching {
                it.icon.svgIcon = commonClient.getWorkItemTypeIcon(it.icon.url)
            }
        }
        
        return responseObject.value
    }

    fun getWorkItem(project: ProjectData, id: Int): WorkItemData?
    {
        val url = getBaseUrl(project.id) + "wit/workItems/${id}"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if (responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.WorkItems)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return null
        }

        return Gson().fromJson(responseBody, WorkItemData::class.java)
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