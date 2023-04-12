package sportisimo.utils

import org.commonmark.node.Code
import org.commonmark.node.Node
import org.commonmark.parser.Parser
import org.commonmark.renderer.NodeRenderer
import org.commonmark.renderer.html.HtmlNodeRendererFactory
import org.commonmark.renderer.html.HtmlRenderer
import org.commonmark.renderer.html.HtmlWriter


object MarkdownHelper
{
    fun toHtml(text: String): String
    {
        val pattern = "`[^`]*`|\\n"
        val cleaned = pattern.toRegex()
            .replace(text) { match -> if (match.value == "\n") "<br>" else match.value }
            //.replace("\n", System.lineSeparator())

        val nodeRenderer = HtmlNodeRendererFactory {
            object: NodeRenderer
            {
                private val html: HtmlWriter = it.writer

                override fun getNodeTypes() = setOf<Class<out Node?>>(Code::class.java)

                override fun render(node: Node?)
                {
                    if (node is Code && node.literal.isNotBlank()) {
                        html.tag("/p")
                        html.tag("code")
                        html.raw(node.literal)
                        html.tag("/code")
                        html.tag("p")
                    }
                }
            }
        }

        val parser = Parser.builder().build()
        val renderer = HtmlRenderer.builder().nodeRendererFactory(nodeRenderer).build()

        val document = parser.parse(cleaned)
        return renderer.render(document)
    }
}