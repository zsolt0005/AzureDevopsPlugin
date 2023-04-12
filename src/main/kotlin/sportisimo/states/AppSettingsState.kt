package sportisimo.states

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import sportisimo.data.ConnectionsData
import sportisimo.data.NotificationStatesData
import sportisimo.data.PullRequestStateData
import sportisimo.data.converters.DataConverters

@State(name = "com.sportisimo.devops.settings.AppSettingsState", storages = [Storage("com.sportisimo.devops.settings.xml")])
class AppSettingsState: PersistentStateComponent<AppSettingsState>
{
    @OptionTag(converter = DataConverters.ConnectionsDataConverter::class)
    var azureConnections: ConnectionsData = ConnectionsData()

    @OptionTag(converter = DataConverters.NotificationStatesDataConverter::class)
    var notificationStates: NotificationStatesData = NotificationStatesData()

    @OptionTag(converter = DataConverters.PullRequestStateDataConverter::class)
    var pullRequestState: PullRequestStateData = PullRequestStateData()

    override fun getState() = this
    override fun loadState(state: AppSettingsState) = XmlSerializerUtil.copyBean(state, this)

    companion object
    {
        private var instance: AppSettingsState? = null

        fun getInstance(): AppSettingsState
        {
            if (instance == null)
            {
                instance = ApplicationManager.getApplication().getService(AppSettingsState::class.java)
            }

            return instance!!
        }
    }
}