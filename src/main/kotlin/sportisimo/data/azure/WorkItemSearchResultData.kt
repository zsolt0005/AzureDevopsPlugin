package sportisimo.data.azure

data class WorkItemSearchResultData(
    val fields: WorkItemSearchResultFieldsData,
    val url: String,
    var svgIcon: String = ""
)