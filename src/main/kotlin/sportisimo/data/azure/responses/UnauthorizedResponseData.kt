package sportisimo.data.azure.responses

data class UnauthorizedResponseData(
    val innerException: String?,
    val message: String,
    val typeName: String,
    val typeKey: String,
    val errorCode: Int,
    val eventId: Int,
)