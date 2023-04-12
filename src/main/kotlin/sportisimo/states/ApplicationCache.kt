package sportisimo.states

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.util.xmlb.XmlSerializerUtil
import sportisimo.data.AvatarIcons

@State(name = "com.sportisimo.devops.ApplicationCache", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class ApplicationCache: PersistentStateComponent<ApplicationCache>
{
    val avatarIcons = AvatarIcons()

    override fun getState() = this

    override fun loadState(state: ApplicationCache) = XmlSerializerUtil.copyBean(state, this)

    companion object
    {
        private var instance: ApplicationCache? = null

        fun getInstance(): ApplicationCache
        {
            if (instance == null)
            {
                instance = ApplicationManager.getApplication().getService(ApplicationCache::class.java)
            }

            return instance!!
        }
    }
}