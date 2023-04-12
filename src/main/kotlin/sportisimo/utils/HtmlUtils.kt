package sportisimo.utils

import com.intellij.openapi.util.text.HtmlBuilder
import com.intellij.openapi.util.text.HtmlChunk

/**
 * Helps working with HTML elements.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object HtmlUtils
{
    /**
     * Creates an HTML bold text string with the given size.
     *
     * @param text
     * @param size Size of the text in em units.
     * @return Bold HTML String.
     */
    fun boldText(text: String, size: Float = 1.0f): String
    {
        return HtmlBuilder()
            .appendRaw("<b style='font-size: ${size}em;'>$text</b>")
            .toString()
    }

    /**
     * Creates an HTML structure for the given body and css.
     *
     * @param body HTML text.
     * @param css List of CSS properties.
     * @return HTML
     */
    fun getHtml(body: String, css: List<Pair<String, Pair<String, String>>>): String
    {
        var cssString = ""
        css.forEach {
            val (property, value) = it.second
            cssString += "${it.first}{${property}: ${value};}"
        }

        val builder = HtmlBuilder()
        builder.append(HtmlChunk.styleTag(cssString)).wrapWith(HtmlChunk.head())
        builder.appendRaw(body).wrapWith(HtmlChunk.body())

        return HtmlBuilder()
            .appendRaw(builder.toString())
            .wrapWith(HtmlChunk.html())
            .toString()
    }
}