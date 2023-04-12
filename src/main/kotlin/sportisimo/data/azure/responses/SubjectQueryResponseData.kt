package sportisimo.data.azure.responses

import sportisimo.data.azure.SubjectQueryResultData

data class SubjectQueryResponseData(
    val count: Int,
    val value: List<SubjectQueryResultData>?
)