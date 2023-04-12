package sportisimo.data.azure.client

data class CompareIterationsSearchData(
    val compareTo: Int,
    val top: Int,
    val skip: Int
): AParamData()
{
    override fun toString(): String
    {
        val builder = StringBuilder()

        addParam(builder, "${"$"}compareTo", compareTo.toString())
        addParam(builder, "${"$"}top", top.toString())
        addParam(builder, "${"$"}skip", skip.toString())

        return builder.toString()
    }
}