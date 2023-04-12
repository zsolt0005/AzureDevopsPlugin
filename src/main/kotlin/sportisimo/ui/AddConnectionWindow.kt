package sportisimo.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.BrowserUtil
import com.intellij.notification.NotificationType
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import sportisimo.azure.Connection
import sportisimo.azure.ScopeHelper
import sportisimo.data.ConnectionData
import sportisimo.data.azure.ProjectData
import sportisimo.events.Events
import sportisimo.renderers.combobox.PlaceholderComboBoxRenderer
import sportisimo.states.AppSettingsState
import sportisimo.utils.NotificationUtils
import sportisimo.utils.StringUtils
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class AddConnectionWindow: DialogWrapper(true)
{
    private val settings = AppSettingsState.getInstance()

    private lateinit var organizationField: Cell<JBTextField>
    private lateinit var azureTokenField: Cell<JBTextField>

    private lateinit var statusLabel: Cell<JLabel>
    private lateinit var projectsComboBox: Cell<ComboBox<ProjectData>>

    private var projects: List<ProjectData> = listOf()

    init
    {
        title = "Add New Azure Connection"
        init()
    }

    override fun createCenterPanel(): JComponent
    {
        okAction.isEnabled = false

        return panel {
            row("Organization: ") {
                organizationField = textField()
                organizationField.component.document.addDocumentListener(onTextChanged())
            }
            row("Azure token: ") {
                azureTokenField = textField()
                azureTokenField.component.document.addDocumentListener(onTextChanged())
            }

            // NEW TOKEN
            twoColumnsRow(
                {
                    link("Generate a new token") {
                        val organization = StringUtils.toUtf8String(organizationField.component.text)

                        val responseCode = testConnection(true)

                        if(responseCode == null || responseCode == 404) return@link

                        BrowserUtil.browse("https://dev.azure.com/$organization/_usersSettings/tokens")
                    }.apply {
                        this.component.toolTipText = ScopeHelper.getRequiredScopesTooltip()
                    }
                },
                {
                    icon(AllIcons.Actions.Help).apply {
                        this.component.toolTipText = ScopeHelper.getRequiredScopesTooltip()
                    }
                    comment("Required permissions").apply {
                        this.component.toolTipText = ScopeHelper.getRequiredScopesTooltip()
                    }
                }
            )

            row {
                button("Test Connection") {
                    testConnection()
                }
            }
            row {
                statusLabel = label(" ")
            }
            row("Project: ") {
                projectsComboBox = comboBox(projects, PlaceholderComboBoxRenderer(""))
                projectsComboBox.component.isEnabled = false
                projectsComboBox.component.addActionListener { onProjectSelected() }
            }
        }
    }

    private fun testConnection(skipToken: Boolean = false): Int?
    {
        resetProjectState()

        val organization = StringUtils.toUtf8String(organizationField.component.text)
        val token = StringUtils.toUtf8String(azureTokenField.component.text)

        val connection = Connection(organization, token)

        var responseCode: Int? = null

        runCatching {
            responseCode = connection.testConnection()
        }.onFailure {
            NotificationUtils.notify(
                "Connection failed",
                "Failed to connect to the Azure API. Error message: ${it.message} <br> $it <br> ${it.cause} <br> ${it.stackTrace.first()}",
                NotificationType.WARNING,
                true,
                isAllowed = settings.notificationStates.testConnection,
                asPopup = true
            )
            return responseCode
        }

        when (responseCode!!)
        {
            200 ->
            {
                statusLabel.component.text = "Connection successful"
                statusLabel.component.foreground = JBColor.GREEN
                onConnectionSuccess(connection)
            }

            404 ->
            {
                statusLabel.component.foreground = JBColor.RED
                statusLabel.component.text = "Organization not found"
            }

            401, 203 ->
            {
                if(skipToken) return responseCode
                statusLabel.component.foreground = JBColor.RED
                statusLabel.component.text = "Authentication failed"
            }

            else ->
            {
                statusLabel.component.foreground = JBColor.RED
                statusLabel.component.text = "Unexpected response with code: $responseCode"
            }
        }

        return responseCode
    }

    override fun doOKAction()
    {
        val printableOrganization = organizationField.component.text

        val organization = StringUtils.toUtf8String(printableOrganization)
        val token = StringUtils.toUtf8String(azureTokenField.component.text)

        val project = projectsComboBox.component.selectedItem!! as ProjectData

        val connectionExists = settings.azureConnections.connections.any {
            it.organization == organization && it.project.id == project.id
        }

        if (connectionExists)
        {
            NotificationUtils.notify(
                "Connection already exists",
                "Connection for the project ${project.name} in the organization $printableOrganization is already in your saved connections.",
                NotificationType.WARNING,
                asPopup = true
            )
        }
        else
        {
            val newConnection = ConnectionData(organization, token, project)
            settings.azureConnections.connections.add(newConnection)
            Events.Application.OnConnectionAdded.fire()

            NotificationUtils.notify(
                "Connection added successfully",
                "Connection for the project ${project.name} in the organization $printableOrganization is added to your connections list.",
                NotificationType.INFORMATION,
                asPopup = true
            )
        }

        super.doOKAction()
    }

    private fun onTextChanged() = object : DocumentListener
    {
        override fun changedUpdate(e: DocumentEvent) = resetFormStatus()
        override fun removeUpdate(e: DocumentEvent) = resetFormStatus()
        override fun insertUpdate(e: DocumentEvent) = resetFormStatus()

        private fun resetFormStatus()
        {
            resetProjectState()
        }
    }

    private fun onConnectionSuccess(connection: Connection)
    {
        resetProjectState()

        runCatching {
            projects = connection.coreClient.getProjects()
        }.onFailure {
            NotificationUtils.notify(
                "Connection failed",
                "Failed to fetch projects. Error message: ${it.message}",
                NotificationType.WARNING,
                isAllowed = settings.notificationStates.testConnection,
                asPopup = true
            )
            return
        }

        projectsComboBox.component.isEnabled = true

        projects.forEach {
            projectsComboBox.component.addItem(it)
        }
    }

    private fun onProjectSelected()
    {
        okAction.isEnabled = true
    }

    private fun resetProjectState()
    {
        projectsComboBox.component.removeAllItems()
        projects = listOf()
        projectsComboBox.component.isEnabled = false
        okAction.isEnabled = false
    }
}
