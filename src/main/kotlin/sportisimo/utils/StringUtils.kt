package sportisimo.utils

import java.nio.charset.Charset

/**
 * Utils for working with strings.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object StringUtils
{
    /**
     * Converts the given value to a UTF8 string.
     *
     * @param value The string to be converted.
     * @return The UTF8 String.
     */
    fun toUtf8String(value: String) = String(value.toByteArray(charset("UTF-8")), Charset.defaultCharset())

    /**
     * Converts the given value from a UTF8 string to the default charset.
     *
     * @param value The string to be converted.
     * @return The string converted to your default charset.
     */
    fun fromUtf8String(value: String) = String(value.toByteArray(Charset.defaultCharset()), charset("UTF-8"))

    /**
     * Cleans the repository name from the prefixes from Azure DevOps
     *
     * @param name The repository name.
     * @return The cleaned repository name.
     */
    fun cleanRepositoryRefName(name: String) = name
        .replace("refs/heads/", "")
        .replace("refs/tags/", "")
        .replace("refs/pull/", "")
}