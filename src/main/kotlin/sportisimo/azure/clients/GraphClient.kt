package sportisimo.azure.clients

import com.google.gson.Gson
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.SubjectQueryResultData
import sportisimo.data.azure.client.SubjectQuerySearchData
import sportisimo.data.azure.responses.StorageKeyResponseData
import sportisimo.data.azure.responses.SubjectQueryResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import sportisimo.utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

class GraphClient(private val connection: Connection)
{
    /**
     * Search users.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun searchUsers(searchData: SubjectQuerySearchData): List<SubjectQueryResultData>
    {
        val url = getBaseUrl() + "graph/subjectQuery"

        val requestBody = Gson().toJson(searchData)
        val (responseCode, responseBody) = connection.doPostRequestAndGetBodyAsString(url, requestBody)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.SubjectQuery)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return listOf()
        }

        val responseObject = Gson().fromJson(responseBody, SubjectQueryResponseData::class.java)

        return responseObject.value ?: listOf()
    }

    fun getSubjectAvatarAsBufferedImage(subjectDescriptor: String, size: String = "medium"): BufferedImage
    {
        val url = getBaseUrl() + "graph/Subjects/$subjectDescriptor/avatars?size=$size&format=png"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsByteArray(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.SubjectAvatar)
            ScopeHelper.notifyMissingScope(missingScope, "", NotificationType.INFORMATION)
            return ImageUtils.iconToBufferedImage(AllIcons.Actions.Stub)
        }

        return ImageIO.read(ByteArrayInputStream(responseBody))
    }

    /**
     * Get the storage key (User ID).
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getStorageKeyByDescriptor(url: String): String? // TODO Provider
    {
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.SubjectQuery)
            ScopeHelper.notifyMissingScope(missingScope, "", NotificationType.INFORMATION)
            return null
        }

        val responseObject = Gson().fromJson(responseBody, StorageKeyResponseData::class.java)

        return responseObject.value
    }

    private fun getBaseUrl(): String
    {
        return "https://vssps.dev.azure.com/${connection.organization}/_apis/"
    }
}