package sportisimo.data.azure.client

data class PullRequestThreadOptionsData(
    val iteration: Int,
    val baseIteration: Int
): AParamData()
{
    override fun toString(): String
    {
        val builder = StringBuilder()

        addParam(builder, "${"$"}iteration", iteration.toString())
        addParam(builder, "${"$"}baseIteration", baseIteration.toString())

        return builder.toString()
    }
}