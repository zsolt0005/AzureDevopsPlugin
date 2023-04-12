package sportisimo.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.util.BackgroundTaskUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.panel
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import sportisimo.azure.Connection
import sportisimo.data.ConnectionData
import sportisimo.data.azure.RepositoryData
import sportisimo.data.azure.RepositoryRefData
import sportisimo.events.Events
import sportisimo.renderers.combobox.PlaceholderComboBoxRenderer
import sportisimo.states.AppSettingsState
import sportisimo.states.ProjectDataState
import sportisimo.utils.ListHelper
import javax.swing.JComponent

// TODO [Low] Length of the data presented when long repo or branch names are used should be handled differently
class SelectConnectionManuallyWindow(private val project: Project): DialogWrapper(true)
{
    private val settings = AppSettingsState.getInstance()

    init
    {
        title = "Select Azure Connection Manually"
        init()
    }

    private lateinit var connectionsComboBox: Cell<ComboBox<ConnectionData>>
    private lateinit var repositoryComboBox: Cell<ComboBox<RepositoryData>>
    private lateinit var branchComboBox: Cell<ComboBox<RepositoryRefData>>
    private lateinit var currentConnection: Connection

    private var repositories: List<RepositoryData> = listOf()
    private var branches: List<RepositoryRefData> = listOf()

    override fun createCenterPanel(): JComponent
    {
        val panel = panel {
            group("Azure DevOps Connection:") {
                row {
                    connectionsComboBox = comboBox(listOf(), PlaceholderComboBoxRenderer(""))
                    connectionsComboBox.component.addActionListener { onConnectionSelected() }
                }
            }
            group("Repository:") {
                row {
                    repositoryComboBox = comboBox(listOf(), PlaceholderComboBoxRenderer(""))
                    repositoryComboBox.component.isEnabled = false
                    repositoryComboBox.component.addActionListener { onRepositorySelected() }

                    AutoCompleteDecorator.decorate(repositoryComboBox.component)
                }
            }
            group("Branch:") {
                row {
                    branchComboBox = comboBox(listOf(), PlaceholderComboBoxRenderer(""))
                    branchComboBox.component.isEnabled = false
                    branchComboBox.component.addActionListener { onBranchSelected() }
                }
            }
        }

        okAction.isEnabled = false

        showSavedConnections()

        return panel
    }

    private fun showSavedConnections()
    {
        connectionsComboBox.component.removeAllItems()

        settings.azureConnections.connections.forEach {
            connectionsComboBox.component.addItem(it)
        }
    }

    private fun onConnectionSelected()
    {
        resetRepositoryData()
        resetBranchData()

        val connectionData = connectionsComboBox.component.selectedItem as ConnectionData? ?: return
        currentConnection = Connection(connectionData.organization, connectionData.token)

        ApplicationManager.getApplication().executeOnPooledThread  {
            showRepositoriesForConnection(connectionData)
        }
    }

    private fun resetBranchData()
    {
        okAction.isEnabled = false

        branchComboBox.component.removeAllItems()
        branches = listOf()
        branchComboBox.component.isEnabled = false
    }

    private fun resetRepositoryData()
    {
        repositoryComboBox.component.removeAllItems()
        repositories = listOf()
        repositoryComboBox.component.isEnabled = false
    }

    private fun showRepositoriesForConnection(connectionData: ConnectionData)
    {
        runCatching {
            repositories = currentConnection.gitClient.getRepositories(connectionData.project).sortedBy {it.name}
        }

        repositoryComboBox.component.removeAllItems()
        repositories.forEach { repositoryComboBox.component.addItem(it) }
        repositoryComboBox.component.isEnabled = true

    }

    private fun onRepositorySelected()
    {
        resetBranchData()

        val repository = repositoryComboBox.component.selectedItem as RepositoryData? ?: return

        ApplicationManager.getApplication().executeOnPooledThread {
            showBranchesForRepository(repository)
        }
    }

    private fun showBranchesForRepository(repositoryData: RepositoryData)
    {
        branches = currentConnection.gitClient.getBranches(repositoryData)
            .filter { it.name.contains("refs/heads") }
            .sortedWith(ListHelper.getRepositorySorter())

        branchComboBox.component.removeAllItems()
        branches.forEach { branchComboBox.component.addItem(it) }
        branchComboBox.component.isEnabled = true
    }

    private fun onBranchSelected()
    {
        branchComboBox.component.selectedItem as RepositoryRefData? ?: return

        okAction.isEnabled = true
    }

    override fun doOKAction()
    {
        val connection = connectionsComboBox.component.selectedItem as ConnectionData? ?: return
        val repository = repositoryComboBox.component.selectedItem as RepositoryData? ?: return
        val branch = branchComboBox.component.selectedItem as RepositoryRefData? ?: return

        val saveData = ProjectDataState.getInstance(project)

        saveData.apply {
            connectionData = connection
            repositoryData = repository
            branchData = branch
            selectedManually = true
        }

        BackgroundTaskUtil.syncPublisher(project, Events.ON_CONNECTION_SELECTED_MANUALLY).onChange()
        PullRequestToolWindowFactory.removeIfExists(project)

        super.doOKAction()
    }
}