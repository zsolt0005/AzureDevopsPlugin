package sportisimo.states

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.OptionTag
import sportisimo.data.cache.GitItemsCacheData
import sportisimo.data.converters.DataConverters

@State(name = "com.sportisimo.devops.ProjectCache", storages = [Storage(StoragePathMacros.CACHE_FILE)])
class ProjectCache: PersistentStateComponent<ProjectCache>
{
    @OptionTag(converter = DataConverters.GitItemsCacheDataConverter::class)
    var gitItems: GitItemsCacheData = GitItemsCacheData()

    fun clearData()
    {
        gitItems = GitItemsCacheData()
    }

    override fun getState() = this

    override fun loadState(state: ProjectCache) = XmlSerializerUtil.copyBean(state, this)

    companion object
    {
        fun getInstance(project: Project): ProjectCache
        {
            return project.getService(ProjectCache::class.java)
        }
    }
}