package sportisimo.utils

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes

/**
 * Helps with the creation of a SerializationStrategy for the Gson library.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object SerializationStrategyUtils
{
    /**
     * Creates an exclusion strategy that excludes the given field names from serialization.
     *
     * @param excludedFieldNames Field names to be excluded.
     * @return The ExclusionStrategy.
     */
    fun getExcludeByFieldNames(excludedFieldNames: List<String>): ExclusionStrategy
    {
        return object: ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes?): Boolean = excludedFieldNames.contains(f?.name)
            override fun shouldSkipClass(clazz: Class<*>?): Boolean = false
        }
    }

    /**
     * Creates an exclusion strategy that excludes the given field types from serialization.
     *
     * @param excludedClasses Field types to be excluded.
     * @return The ExclusionStrategy.
     */
    fun getExcludeByClass(excludedClasses: List<Class<*>>): ExclusionStrategy
    {
        return object: ExclusionStrategy {
            override fun shouldSkipField(f: FieldAttributes?): Boolean = false
            override fun shouldSkipClass(clazz: Class<*>?): Boolean = excludedClasses.contains(clazz)
        }
    }
}