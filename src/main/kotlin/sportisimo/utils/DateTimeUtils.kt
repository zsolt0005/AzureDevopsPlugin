package sportisimo.utils

import java.time.DateTimeException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Working with Date and Time.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object DateTimeUtils
{
    /**
     * Formats the given input to the output format specified.
     *
     * @param input        The input value to be formatted.
     * @param outputFormat The output format.
     * @return The formatted input.
     *
     * @throws DateTimeParseException
     * @throws DateTimeException
     */
    fun format(
        input: String,
        outputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    ): String
    {
        val dateTimeInstant = Instant.parse(input)
        val localDateTime = dateTimeInstant.atZone(ZoneId.of("UTC")).toLocalDateTime()

        return localDateTime.format(outputFormat)
    }
}