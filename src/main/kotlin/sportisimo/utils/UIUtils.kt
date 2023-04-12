package sportisimo.utils

import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.panel
import icons.CustomIcons
import sportisimo.azure.Connection
import sportisimo.data.azure.*
import sportisimo.data.builders.ColumnAlignment
import sportisimo.services.PullRequestThreadService
import sportisimo.threading.ThreadingManager
import sportisimo.ui.builders.PanelBuilder
import sportisimo.ui.elements.EditorPanel
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.FlowLayout
import javax.swing.*

/**
 * For easy work with the common UI elements.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object UIUtils
{
    /**
     * Creates a new JPanel with a FlowLayout.
     *
     * @param isCentered Whether the content should be centered or not.
     * @return The JPanel.
     */
    fun createPanelWithFlowLayout(isCentered: Boolean = false): JPanel
    {
        return JPanel().apply {
            layout = FlowLayout().apply {
                if(isCentered) alignment = FlowLayout.CENTER
            }
        }
    }

    fun createPanelWithBorderLayout(component: JComponent) = JPanel().apply { layout = BorderLayout(); add(component) }

    fun createScrollablePanel(content: JComponent): JPanel
    {
        val panel = JPanel()
            .apply {
                layout = BorderLayout()
            }

        val scrollPanel = JBScrollPane(content, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            .apply {
                border = BorderFactory.createEmptyBorder()
            }

        panel.add(scrollPanel)
        return panel
    }

    fun createLoadingPanel() = PanelBuilder { row { column { loading() } }}.build()

    fun applyPanelToComponent(component: JComponent, panel: JPanel)
    {
        component.removeAll()
        component.add(panel)
        component.revalidate()
    }

    fun createThreadCommentsPanel(
        project: Project,
        azureProject: ProjectData,
        thread: PullRequestThreadData,
        isCollapsible: Boolean,
        currentUser: IdentityData,
        connection: Connection,
        pullRequest: PullRequestData,
        includeJumpToSource: Boolean = false
    ): JPanel
    {
        val service = service<PullRequestThreadService>()
        val width = 500
        var preferredWidth = 0

        val centerActionButtonText = if(thread.isActive()) "Reply & Resolve" else "Reply & Reactivate"
        val centerActionButton = JButton(centerActionButtonText)

        val editorPanel = EditorPanel("Reply", listOf(centerActionButton))
        var actionButton: JButton? = null
        var jumpToSourceButton: JButton? = null

        val cb: Panel.() -> Unit = {
            indent {
                thread.comments.forEachIndexed { i, comment ->
                    val isCommentedByCurrentUser = currentUser.id == comment.author.id

                    val isDeleted = comment.content == null
                    val content = comment.content ?: "• Comment deleted"
                    val isLikedByCurrentUser = comment.usersLiked?.any { it.id == currentUser.id } ?: false
                    val likeIcon = if(isLikedByCurrentUser) CustomIcons.Like else CustomIcons.LikeOutlined

                    var actionsPanel: DialogPanel? = null

                    var likeActionComponent: JComponent? = null
                    var deleteActionComponent: JComponent? = null
                    var editActionComponent: JComponent? = null

                    var commentContent: JEditorPane? = null

                    val commentEditorPanel = EditorPanel("Update").apply {
                        isVisible = false
                        minimumSize = Dimension(width, minimumSize.height)
                    }

                    val threadPanel = com.intellij.ui.dsl.builder.panel {
                        if(!isDeleted)
                        {
                            twoColumnsRow(
                                {
                                    icon(comment.author.getImageIcon()).gap(RightGap.SMALL)
                                    text("<b>${comment.author.displayName}</b>")
                                    comment(DateTimeUtils.format(comment.publishedDate))
                                },
                                {
                                    if(comment.usersLiked?.isNotEmpty() == true)
                                    {
                                        likeActionComponent = icon(likeIcon).gap(RightGap.SMALL).component
                                        label(comment.usersLiked.size.toString())
                                    }

                                    actionsPanel = com.intellij.ui.dsl.builder.panel {
                                        row {
                                            if(comment.usersLiked?.isEmpty() != false)
                                            {
                                                likeActionComponent = icon(likeIcon).gap(RightGap.SMALL).component
                                            }

                                            if(isCommentedByCurrentUser)
                                            {
                                                editActionComponent = icon(AllIcons.Actions.Edit).gap(RightGap.SMALL).component
                                                deleteActionComponent = icon(AllIcons.Actions.GC).gap(RightGap.SMALL).component
                                            }
                                        }
                                        gap(RightGap.SMALL)
                                    }
                                    cell(actionsPanel!!)

                                    if(i == 0)
                                    {
                                        val statusPanel = com.intellij.ui.dsl.builder.panel {
                                            row {
                                                comboBox(PullRequestThreadData.getDisplayStatuses()).apply {
                                                    this.component.selectedItem = thread.getDisplayStatus()
                                                    this.component.addActionListener {
                                                        val selectedStatus = this.component.selectedItem as String
                                                        val actualStatus = PullRequestThreadData.getStatusFromDisplayStatus(selectedStatus)
                                                        service.updateThreadStatus(project, connection, azureProject, pullRequest, thread, actualStatus)
                                                    }
                                                }
                                            }
                                        }
                                        cell(statusPanel)
                                    }
                                }
                            )
                        }
                        indent {
                            row {
                                val htmlContent = MarkdownHelper.toHtml(content)

                                val innerPanel = PanelBuilder {
                                    row {
                                        column {
                                            commentContent = text(htmlContent, true).apply {
                                                this.maximumSize = Dimension(width, this.maximumSize.height)
                                            }
                                            add(commentEditorPanel)
                                        }
                                    }
                                }.build()

                                commentEditorPanel.addOnTextAreaUpdated {
                                    innerPanel.minimumSize = Dimension(width, commentEditorPanel.preferredSize.height)
                                    innerPanel.revalidate()
                                }

                                cell(innerPanel)
                            }
                        }
                    }
                    row { cell(threadPanel) }
                    preferredWidth = threadPanel.preferredSize.width

                    actionsPanel?.isVisible = false

                    threadPanel.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
                        onMouseEntered = { actionsPanel?.isVisible = true },
                        onMouseExited = {
                            if (threadPanel.contains(it?.point)) return@onAnyMouseEvents
                            actionsPanel?.isVisible = false
                        }
                    ))

                    likeActionComponent?.cursor = Cursor(Cursor.HAND_CURSOR)
                    deleteActionComponent?.cursor = Cursor(Cursor.HAND_CURSOR)
                    editActionComponent?.cursor = Cursor(Cursor.HAND_CURSOR)

                    var likesToolTipText = ""
                    comment.usersLiked?.forEachIndexed { userIndex, user ->
                        if(userIndex != 0) likesToolTipText += "<br>"
                        likesToolTipText += user.displayName
                    }
                    likeActionComponent?.toolTipText = likesToolTipText

                    likeActionComponent?.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
                        onMouseClicked = {
                            service.addLikeToComment(project, connection, azureProject, pullRequest, thread, comment, currentUser)
                        }
                    ))

                    deleteActionComponent?.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
                        onMouseClicked = {
                            ConfirmDialogUtils.confirm(
                                "Delete comment",
                                "Are you sure you want to delete this comment?"
                            ) {
                                service.deleteComment(project, connection, azureProject, pullRequest, thread, comment)
                            }
                        }
                    ))

                    editActionComponent?.addMouseListener(EventUtils.MouseEvents.onAnyMouseEvents(
                        onMouseClicked = {
                            commentContent!!.isVisible = false

                            commentEditorPanel.setText(content)
                            commentEditorPanel.isVisible = true
                        }
                    ))

                    commentEditorPanel.addOnReplyClicked {
                        val htmlContent = MarkdownHelper.toHtml(content)
                        commentContent!!.text = htmlContent
                        commentContent!!.isVisible = true
                        commentEditorPanel.isVisible = false

                        service.updateThreadComment(project, connection, azureProject, pullRequest, thread, comment, it)
                    }

                    commentEditorPanel.addOnCancelClicked {
                        commentContent!!.isVisible = true
                        commentEditorPanel.isVisible = false
                    }

                    separator()
                }

                row {
                    val bottomPanel = PanelBuilder {
                        row {
                            column {
                                panel {
                                    row { column {
                                        asyncIcon(currentUser.getImageIconAsync())
                                    } }
                                    rowFiller()
                                }.apply {
                                    this.maximumSize = Dimension(24, Int.MAX_VALUE) // TODO Hardcoded value of the icon panel size
                                }
                            }
                            column {
                                add(editorPanel)
                            }
                            column {
                                val actionText = if(thread.isActive()) "Resolve" else "Reactivate"
                                val newStatus = if(thread.isActive()) PullRequestThreadStatus.Resolved else PullRequestThreadStatus.Active

                                panel {
                                    row { column {
                                        actionButton = button(actionText) {
                                            service.updateThreadStatus(project, connection, azureProject, pullRequest, thread, newStatus)
                                        }
                                    } }
                                    rowFiller()
                                }.apply {
                                    this.maximumSize = Dimension(actionButton!!.preferredSize.width, Int.MAX_VALUE)
                                }
                            }

                            if(includeJumpToSource && thread.threadContext!!.leftFileStart == null)
                            {
                                column(ColumnAlignment.Right) {
                                    panel {
                                        row { column {
                                            jumpToSourceButton = button("Jump to source", AllIcons.Actions.EditSource) {
                                                val path = thread.threadContext.filePath
                                                val line = thread.threadContext.rightFileStart?.line ?: return@button

                                                val file = VirtualFileManager.getInstance().findFileByUrl("file://${project.basePath}${path}") ?: return@button
                                                val descriptor = OpenFileDescriptor(project, file, line - 1, 0)

                                                ThreadingManager.executeOnDispatchThreadAndAwaitResult {
                                                    FileEditorManager.getInstance(project).openEditor(descriptor, true).firstOrNull() ?: return@executeOnDispatchThreadAndAwaitResult
                                                }
                                            }
                                        } }
                                        rowFiller()
                                    }.apply {
                                        this.maximumSize = Dimension(jumpToSourceButton!!.preferredSize.width, Int.MAX_VALUE)
                                    }
                                }
                            }
                        }
                    }.build().apply {
                        preferredSize = Dimension(preferredWidth, preferredSize.height)
                    }

                    cell(bottomPanel)

                    editorPanel.addOnTextAreaUpdated {
                        bottomPanel.preferredSize = Dimension(preferredWidth, editorPanel.preferredSize.height)
                        bottomPanel.revalidate()
                    }
                }
            }
        }

        editorPanel.addOnReplyClicked {
            service.addThreadComment(project, connection, azureProject, pullRequest, thread, it)
        }

        centerActionButton.addActionListener {
            val content = editorPanel.getText()
            service.addThreadComment(project, connection, azureProject, pullRequest, thread, content)

            val newStatus = if(thread.isActive()) PullRequestThreadStatus.Resolved else PullRequestThreadStatus.Active
             service.updateThreadStatus(project, connection, azureProject, pullRequest, thread, newStatus)
        }

        return panel {
            val fileName = FileUtils.getFileNameFromPath(thread.threadContext!!.filePath)
            val filePath = thread.threadContext.filePath

            val title = HtmlUtils.getHtml("<b>$fileName</b> <span style='color: #8c9496;'>$filePath</span> <b>[${thread.getDisplayStatus()}]</b>", listOf())
            if(isCollapsible)
            {
                collapsibleGroup(title, false, cb).apply {
                    if(thread.isActive())
                    {
                        this.expanded = true
                    }
                }
            }
            else
            {
                group(title, false, cb)
            }
        }
    }
}