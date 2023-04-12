package sportisimo.data.azure.client

import com.google.gson.annotations.SerializedName

data class WorkItemSearchData(
    val searchText: String,
    @SerializedName("${'$'}skip")val skip: Int = 0,
    @SerializedName("${'$'}top")val top: Int = 1
)