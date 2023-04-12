package sportisimo.azure

import com.google.gson.Gson
import sportisimo.azure.clients.*
import sportisimo.data.ConnectionData
import sportisimo.data.azure.responses.UnauthorizedResponseData
import sportisimo.exceptions.EmptyResponseBodyException
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandler
import java.util.*

class Connection(val organization: String, token: String)
{
    constructor(connectionData: ConnectionData) : this(connectionData.organization, connectionData.token)

    private val apiVersion = "7.1-preview"
    private var base64Token: String

    init { base64Token = Base64.getEncoder().encodeToString(":$token".toByteArray()) }

    var commonClient: CommonClient = CommonClient(this)
    var coreClient: CoreClient = CoreClient(this)
    var gitClient: GitClient = GitClient(this)
    var searchClient: SearchClient = SearchClient(this)
    var workItemClient: WorkItemClient = WorkItemClient(this)
    var graphClient: GraphClient = GraphClient(this)
    var buildClient: BuildClient = BuildClient(this)

    /**
     * * **203** -> bad token
     * * **401** -> bad token
     * * **404** -> bad organization
     * * **200** -> OK
     */
    fun testConnection(): Int
    {
        val url = "https://dev.azure.com/$organization/_apis/projects"

        val (responseCode, _) = doGetRequestAndGetBodyAsString(url)

        return responseCode
    }

    /**
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun doGetRequestAndGetBodyAsString(url: String, accept: String = Accept.JSON.value): Pair<Int, String>
    {
        val (statusCode, body) = doGetRequestAndGetBody(url, HttpResponse.BodyHandlers.ofString(), accept)
        return prepareStringResponseCodeToBodyPair(statusCode, body)
    }

    /**
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun doGetRequestAndGetBodyAsByteArray(url: String, accept: String = Accept.JSON.value): Pair<Int, ByteArray>
    {
        val (statusCode, body) = doGetRequestAndGetBody(url, HttpResponse.BodyHandlers.ofByteArray(), accept)

        if(body.isEmpty() && statusCode != 401)
        {
            throw EmptyResponseBodyException("Request to the url $url returned with empty body and response code $statusCode")
        }

        return statusCode to body
    }

    /**
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     * @throws EmptyResponseBodyException If the response body is empty.
     */
    fun doPostRequestAndGetBodyAsString(url: String, requestBody: String, accept: String = Accept.JSON.value): Pair<Int, String>
    {
        val (statusCode, body) = doPostRequestAndGetBody(url, requestBody, HttpResponse.BodyHandlers.ofString(), accept)
        return prepareStringResponseCodeToBodyPair(statusCode, body)
    }

    fun doPatchRequestAndGetBodyAsString(url: String, requestBody: String, accept: String = Accept.JSON.value): Pair<Int, String>
    {
        val (statusCode, body) = doPatchRequestAndGetBody(url, requestBody, HttpResponse.BodyHandlers.ofString(), accept)
        return prepareStringResponseCodeToBodyPair(statusCode, body)
    }

    fun doPutRequestAndGetBodyAsString(url: String, requestBody: String, accept: String = Accept.JSON.value): Pair<Int, String>
    {
        val (statusCode, body) = doPutRequestAndGetBody(url, requestBody, HttpResponse.BodyHandlers.ofString(), accept)
        return prepareStringResponseCodeToBodyPair(statusCode, body)
    }

    fun doDeleteRequestAndGetBodyAsString(url: String, accept: String = Accept.JSON.value): Pair<Int, String>
    {
        val (statusCode, body) = doDeleteRequestAndGetBody(url, HttpResponse.BodyHandlers.ofString(), accept)
        return prepareStringResponseCodeToBodyPair(statusCode, body)
    }

    private fun prepareStringResponseCodeToBodyPair(statusCode: Int, body: String): Pair<Int, String>
    {
        var responseBody = body

        if(body.isNotEmpty() && statusCode == 401)
        {
            val responseObject = Gson().fromJson(body, UnauthorizedResponseData::class.java)
            responseBody = responseObject.message
        }

        return statusCode to responseBody
    }

    /**
     * @throws IOException If an I/O error occurs when sending or receiving.
     * @throws InterruptedException If the operation is interrupted.
     */
    private fun <T>doGetRequestAndGetBody(url: String, bodyHandler: BodyHandler<T>, accept: String): Pair<Int, T>
    {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = prepareGetRequest(url, accept)

        val response = client.send(request, bodyHandler)

        val body = response.body()

        return response.statusCode() to body
    }

    private fun <T>doPostRequestAndGetBody(url: String, requestBody: String, bodyHandler: BodyHandler<T>, accept: String): Pair<Int, T>
    {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = preparePostRequest(url, requestBody, accept)

        val response = client.send(request, bodyHandler)

        val body = response.body()

        return response.statusCode() to body
    }

    private fun <T>doPatchRequestAndGetBody(url: String, requestBody: String, bodyHandler: BodyHandler<T>, accept: String): Pair<Int, T>
    {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = preparePatchRequest(url, requestBody, accept)

        val response = client.send(request, bodyHandler)

        val body = response.body()

        return response.statusCode() to body
    }

    private fun <T>doPutRequestAndGetBody(url: String, requestBody: String, bodyHandler: BodyHandler<T>, accept: String): Pair<Int, T>
    {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = preparePutRequest(url, requestBody, accept)

        val response = client.send(request, bodyHandler)

        val body = response.body()

        return response.statusCode() to body
    }

    private fun <T>doDeleteRequestAndGetBody(url: String, bodyHandler: BodyHandler<T>, accept: String): Pair<Int, T>
    {
        val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build()

        val request = prepareDeleteRequest(url, accept)

        val response = client.send(request, bodyHandler)

        val body = response.body()

        return response.statusCode() to body
    }

    private fun prepareGetRequest(url: String, accept: String): HttpRequest
    {
        return prepareRequest(url, accept).GET().build()
    }

    private fun preparePostRequest(url: String, requestBody: String, accept: String): HttpRequest
    {
        return prepareRequest(url, accept).POST(HttpRequest.BodyPublishers.ofString(requestBody)).build()
    }

    private fun preparePatchRequest(url: String, requestBody: String, accept: String): HttpRequest
    {
        return prepareRequest(url, accept).method("PATCH", HttpRequest.BodyPublishers.ofString(requestBody)).build()
    }

    private fun preparePutRequest(url: String, requestBody: String, accept: String): HttpRequest
    {
        return prepareRequest(url, accept).PUT(HttpRequest.BodyPublishers.ofString(requestBody)).build()
    }

    private fun prepareDeleteRequest(url: String, accept: String): HttpRequest
    {
        return prepareRequest(url, accept).DELETE().build()
    }

    private fun prepareRequest(url: String, accept: String): HttpRequest.Builder
    {
        return HttpRequest.newBuilder()
            .uri(URI(url))
            .headers(
                "Accept", "$accept;api-version=$apiVersion",
                "Authorization", "Basic $base64Token",
                "Content-Type", "application/json"
            )
    }
}

enum class Accept(val value: String)
{
    JSON("application/json"),
    SVG("image/svg+xml")
}