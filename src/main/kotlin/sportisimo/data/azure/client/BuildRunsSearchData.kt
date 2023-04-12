package sportisimo.data.azure.client

data class BuildRunsSearchData(
    val limit: Int? = null,
    val branchName: String? = null,
    val queryOrder: QueryOrder? = null
): AParamData()
{
    override fun toString(): String
    {
        val builder = StringBuilder()

        addParam(builder, "${"$"}top", limit?.toString())
        addParam(builder, "branchName", branchName)
        addParam(builder, "queryOrder", queryOrder?.value)

        return builder.toString()
    }
}

enum class QueryOrder(val value: String)
{
    FinishTimeAscending("finishTimeAscending"),
    FinishTimeDescending("finishTimeDescending"),
    QueueTimeAscending("queueTimeAscending"),
    QueueTimeDescending("queueTimeDescending"),
    StartTimeAscending("startTimeAscending"),
    StartTimeDescending("startTimeDescending")
}