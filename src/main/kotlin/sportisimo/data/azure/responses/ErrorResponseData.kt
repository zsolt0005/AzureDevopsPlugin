package sportisimo.data.azure.responses

data class ErrorResponseData(
    val message: String,
    val typeName: String,
    val typeKey: String,
    val errorCode: Int,
    val eventId: Int
)