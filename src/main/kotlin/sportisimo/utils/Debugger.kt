package sportisimo.utils

object Debugger
{
    fun debugProduction(message: String)
    {
        NotificationUtils.notify(
            "Debugger:debugProduction",
            message
        )
    }
}