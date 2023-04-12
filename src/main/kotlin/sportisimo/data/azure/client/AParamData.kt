package sportisimo.data.azure.client

abstract class AParamData
{
    protected fun addParam(builder: StringBuilder, name: String, value: String?)
    {
        if(value == null) return

        if(builder.isNotEmpty()) builder.append("&")
        builder.append("$name=$value")
    }
}