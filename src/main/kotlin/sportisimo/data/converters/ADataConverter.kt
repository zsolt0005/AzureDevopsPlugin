package sportisimo.data.converters

import com.google.gson.Gson
import com.intellij.util.xmlb.Converter

abstract class ADataConverter<T : Any>: Converter<T>()
{
    override fun toString(value: T): String
    {
        return Gson().toJson(value)
    }
}