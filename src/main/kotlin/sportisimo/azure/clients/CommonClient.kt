package sportisimo.azure.clients

import com.google.gson.Gson
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationType
import sportisimo.azure.Accept
import sportisimo.azure.Connection
import sportisimo.azure.Scope
import sportisimo.azure.ScopeHelper
import sportisimo.data.azure.IdentityData
import sportisimo.data.azure.responses.AuthorizedUserResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import sportisimo.utils.ImageUtils
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.IOException
import javax.imageio.ImageIO

class CommonClient(private val connection: Connection)
{
    fun getAvatarAsBufferedImage(url: String): BufferedImage // TODO Provider
    {
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsByteArray(url)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.AvatarIcons)
            ScopeHelper.notifyMissingScope(missingScope, "", NotificationType.INFORMATION)
            return ImageUtils.iconToBufferedImage(AllIcons.Actions.Stub)
        }

        return ImageIO.read(ByteArrayInputStream(responseBody))
    }

    fun getAuthenticatedUserInformation(): IdentityData? // TODO Provider **
    {
        val url = getBaseUrl() + "connectionData"
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url)

        if (responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.NoScope)
            ScopeHelper.notifyMissingScope(missingScope, responseBody)
            return null
        }

        val responseObject = Gson().fromJson(responseBody, AuthorizedUserResponseData::class.java)

        return responseObject.authenticatedUser
    }

    /**
     * Gets the work item type icons.
     *
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun getWorkItemTypeIcon(url: String): String
    {
        val (responseCode, responseBody) = connection.doGetRequestAndGetBodyAsString(url, Accept.SVG.value)

        if(responseCode == 401)
        {
            val missingScope = ScopeHelper.getScopeByName(Scope.WorkItemTypeIcons)
            ScopeHelper.notifyMissingScope(missingScope, "", NotificationType.INFORMATION)
            return ""
        }

        return responseBody
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