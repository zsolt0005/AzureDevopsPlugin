package sportisimo.ui

import com.intellij.notification.NotificationType
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.dsl.builder.Cell
import com.intellij.ui.dsl.builder.TopGap
import com.intellij.ui.dsl.builder.panel
import sportisimo.data.ConnectionData
import sportisimo.events.Events
import sportisimo.renderers.combobox.PlaceholderComboBoxRenderer
import sportisimo.states.AppSettingsState
import sportisimo.utils.ConfirmDialogUtils
import sportisimo.utils.NotificationUtils
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JOptionPane

class SettingsPage : Configurable
{
    private val settings = AppSettingsState.getInstance()

    private lateinit var connectionsComboBox: Cell<ComboBox<ConnectionData>>
    private lateinit var removeConnectionButton: Cell<JButton>

    override fun createComponent(): JComponent
    {
        val panel = panel {
            group("Azure Settings") {
                twoColumnsRow(
                    {
                        this.label("Connections: ")
                        connectionsComboBox = comboBox(listOf(), PlaceholderComboBoxRenderer("-"))

                        removeConnectionButton = button("Remove") {
                            onRemoveButtonPressed()
                        }
                    },
                    {
                        button("Add New Connection") {
                            AddConnectionWindow().show()
                            showSavedConnections()
                        }
                    }
                )
            }

            collapsibleGroup("Notifications") {
                twoColumnsRow(
                    {
                        spinner(IntRange(1, 3), 1).apply {
                            this.component.value = settings.notificationStates.notificationLevel
                            this.component.addChangeListener {
                                settings.notificationStates.notificationLevel = this.component.value as Int
                            }
                        }
                        label("Notification level")
                    },
                    {
                        comment("Limit the notifications across the plugin. (3 - INFO | 2 - WARNING | 1 - ERROR)")
                    }
                )
                twoColumnsRow(
                    {
                        topGap(TopGap.SMALL)
                        checkBox("Test connection").apply {
                            this.component.isSelected = settings.notificationStates.testConnection
                            this.component.addItemListener {
                                settings.notificationStates.testConnection = this.component.isSelected
                            }
                        }
                    },
                    {
                        comment("Notifications showed when test connection button is pressed")
                    }
                )
                twoColumnsRow(
                    {
                        checkBox("Tool window initialization").apply {
                            this.component.isSelected = settings.notificationStates.toolWindowInitialization
                            this.component.addItemListener {
                                settings.notificationStates.toolWindowInitialization = this.component.isSelected
                            }
                        }
                    },
                    {
                        comment("Notifications showed when the tool window is initialized or refreshed")
                    }
                )
                twoColumnsRow(
                    {
                        checkBox("Missing REQUIRED permissions").apply {
                            this.component.isSelected = settings.notificationStates.missingRequiredScope
                            this.component.addItemListener {
                                settings.notificationStates.missingRequiredScope = this.component.isSelected
                            }
                        }
                    },
                    {
                        comment("Notifications showed when there is a missing REQUIRED permission (scope)")
                    }
                )
                twoColumnsRow(
                    {
                        checkBox("Missing OPTIONAL permissions").apply {
                            this.component.isSelected = settings.notificationStates.missingOptionalScope
                            this.component.addItemListener {
                                settings.notificationStates.missingOptionalScope = this.component.isSelected
                            }
                        }
                    },
                    {
                        comment("Notifications showed when there is a missing OPTIONAL permission (scope)")
                    }
                )
                twoColumnsRow(
                    {
                        checkBox("Local git").apply {
                            this.component.isSelected = settings.notificationStates.localGit
                            this.component.addItemListener {
                                settings.notificationStates.localGit = this.component.isSelected
                            }
                        }
                    },
                    {
                        comment("Notifications about the local GIT, eg. 'Repository remote not found'")
                    }
                )
            }

            collapsibleGroup("Pull Requests") {
                twoColumnsRow(
                    {
                        checkBox("Abandoned pull requests").apply {
                            this.component.isSelected = settings.pullRequestState.abandonedPullRequests
                            this.component.addItemListener {
                                settings.pullRequestState.abandonedPullRequests = this.component.isSelected
                            }
                        }
                    },
                    {comment("Whether to show abandoned pull requests.")}
                )
                twoColumnsRow(
                    {
                        checkBox("Make reviewers flagged").apply {
                            this.component.isSelected = settings.pullRequestState.makeReviewerFlagged
                            this.component.addItemListener {
                                settings.pullRequestState.makeReviewerFlagged = this.component.isSelected
                            }
                        }
                    },
                    {comment("When a reviewer interacts with the pull request through the plugin, set them as flagged. (Visible to the right of the users name)")}
                )
                twoColumnsRow(
                    {
                        checkBox("Hide not active PR comments in editor").apply {
                            this.component.isSelected = settings.pullRequestState.hideNotActiveComments
                            this.component.addItemListener {
                                settings.pullRequestState.hideNotActiveComments = this.component.isSelected
                            }
                        }
                    },
                    {comment("Hides all comments that are not in Active or Pending status from the editor.")}
                )

                /*twoColumnsRow(
                    {
                        spinner(IntRange(0, 120), 5).apply {
                            this.component.value = settings.pullRequestState.pullRequestAutoRefreshFrequency
                            this.component.addChangeListener {
                                settings.pullRequestState.pullRequestAutoRefreshFrequency = this.component.value as Int
                            }
                        }
                        label("Pull request auto refresh frequency")
                    },
                    {comment("How often will be the pull request refreshed automatically in SECONDS. 0 = Never")}
                )
                twoColumnsRow(
                    {
                        spinner(IntRange(0, 120), 5).apply {
                            this.component.value = settings.pullRequestState.pullRequestThreadsAutoRefreshFrequency
                            this.component.addChangeListener {
                                settings.pullRequestState.pullRequestThreadsAutoRefreshFrequency = this.component.value as Int
                            }
                        }
                        label("Pull request COMMENTS auto refresh frequency")
                    },
                    {comment("How often will be the pull request COMMENTS refreshed automatically in SECONDS. 0 = Never")}
                )*/
            }

            row {
                topGap(TopGap.MEDIUM)
                text("<b style='color: #fc9292; text-align: right;'>All settings are saved on the fly</b>")
            }
        }

        showSavedConnections()

        return panel
    }

    private fun onRemoveButtonPressed()
    {
        val selectedConnection = connectionsComboBox.component.selectedItem as ConnectionData? ?: return

        deleteSavedConnection(selectedConnection)
    }

    override fun isModified(): Boolean = false

    override fun apply() {}

    override fun getDisplayName() = "Azure DevOps Integration"

    private fun showSavedConnections()
    {
        connectionsComboBox.component.removeAllItems()
        removeConnectionButton.visible(false)

        settings.azureConnections.connections.forEach {
            connectionsComboBox.component.addItem(it)
        }

        removeConnectionButton.visible(true)
    }

    private fun deleteSavedConnection(connection: ConnectionData)
    {
        val confirmation = ConfirmDialogUtils.yesNo(
            "Delete Connection",
            "Delete connection for the project ${connection.project.name} in the organization ${connection.organization}?"
        )

        if (confirmation != JOptionPane.YES_OPTION)
        {
            return
        }

        settings.azureConnections.connections.remove(connection)
        Events.Application.OnConnectionRemoved.fire()

        NotificationUtils.notify(
            "Connection deleted successfully",
            "Connection for the project ${connection.project.name} in the organization ${connection.organization} was deleted.",
            NotificationType.INFORMATION,
            asPopup = true
        )

        showSavedConnections()
    }
}