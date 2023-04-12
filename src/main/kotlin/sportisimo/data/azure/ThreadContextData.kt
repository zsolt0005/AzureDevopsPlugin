package sportisimo.data.azure

data class ThreadContextData(
    val filePath: String,
    val leftFileStart: ThreadContextPositionData? = null,
    val leftFileEnd: ThreadContextPositionData? = null,
    val rightFileStart: ThreadContextPositionData? = null,
    val rightFileEnd: ThreadContextPositionData? = null,
)

data class ThreadContextPositionData(
    val line: Int,
    val offset: Int
)