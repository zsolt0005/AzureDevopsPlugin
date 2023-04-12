package sportisimo.ui.builders

import com.intellij.BundleBase
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.JBColor
import com.intellij.ui.components.*
import com.intellij.ui.dsl.builder.components.DslLabel
import com.intellij.ui.dsl.builder.components.DslLabelType
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBFont
import com.intellij.util.ui.JBUI
import sportisimo.data.builders.ColumnContentData
import sportisimo.data.ui.HorizontalGap
import sportisimo.renderers.combobox.PlaceholderComboBoxRenderer
import sportisimo.threading.FutureNotice
import sportisimo.ui.elements.ListPanel
import sportisimo.ui.elements.adapters.AListPanelAdapter
import sportisimo.ui.elements.adapters.ListPanelStringAdapter
import sportisimo.utils.EventUtils
import java.awt.Color
import java.awt.Component
import java.awt.Font
import java.awt.event.ActionEvent
import java.util.*
import java.util.concurrent.Future
import javax.swing.*
import javax.swing.event.DocumentEvent

class ColumnBuilder
{
    val contentData: MutableList<ColumnContentData> = mutableListOf()

    fun add(component: Component?, gap: HorizontalGap? = null, builder: PanelBuilder? = null)
    {
        if(gap != null) horizontalGap(gap.left)
        contentData.add(ColumnContentData(component, builder))
        if(gap != null) horizontalGap(gap.right)
    }

    fun label(
        text: String,
        isBold: Boolean = false,
        color: Color = JBColor.foreground(),
        gap: HorizontalGap = HorizontalGap(0, 0)
    ): JBLabel
    {
        val label = JBLabel(text).apply {
            if(isBold) font = font.deriveFont(Font.BOLD)

            foreground = color
        }

        add(label, gap)
        return label
    }

    fun boldLabel(text: String, gap: HorizontalGap = HorizontalGap(0, 0)) = label(text, true, gap = gap)

    fun comment(text: String, gap: HorizontalGap = HorizontalGap(0, 0)) = label(text, false, JBUI.CurrentTheme.ContextHelp.FOREGROUND, gap)

    fun boldComment(text: String, gap: HorizontalGap = HorizontalGap(0, 0)) = label(text, true, JBUI.CurrentTheme.ContextHelp.FOREGROUND, gap)

    fun link(text: String, gap: HorizontalGap = HorizontalGap(0, 0), action: (ActionEvent) -> Unit): ActionLink
    {
        val link = ActionLink(text, action)

        add(link, gap)
        return link
    }

    fun textArea(rows: Int, gap: HorizontalGap = HorizontalGap(0, 0)): JBTextArea
    {
        val textArea = JBTextArea().apply {
            border = JBEmptyBorder(3, 5, 3, 5)
            font = JBFont.regular()
            lineWrap = true
            wrapStyleWord = true

            this.rows = rows
        }

        val pane = JBScrollPane(textArea, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER).apply {
            minimumSize = textArea.preferredSize
        }

        add(pane, gap)
        return textArea
    }

    fun expandableTextArea(gap: HorizontalGap = HorizontalGap(0, 0), onUpdated: (DocumentEvent) -> Unit): JBTextArea
    {
        val textArea = JBTextArea().apply {
            border = JBUI.Borders.customLine(JBColor.border())
            font = JBFont.regular()
            lineWrap = true
            wrapStyleWord = true
            rows = 1
        }

        textArea.document.addDocumentListener(EventUtils.DocumentEvents.onAllDocumentEvent {
            onUpdated(it)
        })

        add(textArea, gap)
        return textArea
    }

    fun textField(gap: HorizontalGap = HorizontalGap(0, 0)): JBTextField
    {
        val textField = JBTextField().apply {
            border = JBUI.Borders.customLine(JBColor.border())
        }

        add(textField, gap)
        return textField
    }

    fun searchTextField(gap: HorizontalGap = HorizontalGap(0, 0)): JBTextField
    {
        val textField = JBTextField()
        val icon = JBLabel(AllIcons.Actions.Search).apply {
            border = JBUI.Borders.empty(0, 4)
        }

        val container = JPanel().apply {
            border = JBUI.Borders.customLine(JBColor.border())
            background = textField.background
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(textField)
            add(icon)
        }

        textField.apply {
            border = JBUI.Borders.empty()
            margin = JBUI.emptyInsets()
            isOpaque = true
        }

        add(container, gap)
        return textField
    }

    fun <T>comboBox(
        items: List<T>,
        placeholder: String = "",
        gap: HorizontalGap = HorizontalGap(0, 0)
    ): ComboBox<T>
    {
        val comboBox = ComboBox(DefaultComboBoxModel(Vector(items))).apply {
            this.renderer = PlaceholderComboBoxRenderer(placeholder)
            border = JBUI.Borders.customLine(JBColor.border())
            background = JBColor.background()
        }

        add(comboBox, gap)
        return comboBox
    }

    fun checkBox(text: String? = null, gap: HorizontalGap = HorizontalGap(0, 0)): JBCheckBox
    {
        val checkBox = JBCheckBox(text)

        add(checkBox, gap)
        return checkBox
    }

    fun panel(gap: HorizontalGap = HorizontalGap(0, 0), builder: PanelBuilder.() -> Unit = {}): JPanel
    {
        val panelBuilder = PanelBuilder(builder)
        val content = panelBuilder.build()

        add(content, gap)
        return content
    }

    fun text(text: String, isFocusable: Boolean = false, gap: HorizontalGap = HorizontalGap(0, 0)): DslLabel
    {
        val dslLabel = DslLabel(DslLabelType.LABEL)
        dslLabel.maxLineLength = Int.MAX_VALUE - 1
        dslLabel.text = text
        dslLabel.isFocusable = isFocusable

        add(dslLabel, gap)

        return dslLabel
    }

    fun <T>listPanel(
        list: List<T>,
        rows: Int = 4,
        adapter: AListPanelAdapter = ListPanelStringAdapter(),
        gap: HorizontalGap = HorizontalGap(0, 0)
    ): ListPanel<T>
    {
        val panel = ListPanel(list, rows, adapter)

        add(panel, gap)
        return panel
    }

    fun button(text: String, icon: Icon? = null, gap: HorizontalGap = HorizontalGap(0, 0), actionListener: (event: ActionEvent) -> Unit): JButton
    {
        val button = JButton(BundleBase.replaceMnemonicAmpersand(text), icon)
        button.addActionListener(actionListener)

        add(button, gap)

        return button
    }

    fun icon(icon: Icon, gap: HorizontalGap = HorizontalGap(0, 0)): JBLabel
    {
        val label = JBLabel(icon)

        add(label, gap)
        return label
    }

    fun asyncIcon(future: Future<Icon>?, gap: HorizontalGap = HorizontalGap(0, 0)): JBLabel
    {
        val label = JBLabel(null)

        if(future != null)
        {
            FutureNotice(future)
                .onSuccess { label.icon = it }
                .awaitCompletion()
        }

        add(label, gap)
        return label
    }

    fun horizontalGap(size: Int)
    {
        add(Box.createHorizontalStrut(size), null)
    }

    fun loading(gap: HorizontalGap = HorizontalGap(0, 0)): JBLabel
    {
        val animatedLoadingIcon = AnimatedIcon(
            125,
            AllIcons.Process.Step_1,
            AllIcons.Process.Step_2,
            AllIcons.Process.Step_3,
            AllIcons.Process.Step_4,
            AllIcons.Process.Step_5,
            AllIcons.Process.Step_6,
            AllIcons.Process.Step_7,
            AllIcons.Process.Step_8
        )
        val loading = JBLabel(animatedLoadingIcon)

        add(loading, gap)
        return loading
    }
}
