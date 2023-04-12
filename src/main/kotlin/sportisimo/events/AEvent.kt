package sportisimo.events

import com.intellij.openapi.project.Project
import sportisimo.events.listeners.AChangeListener

abstract class AEvent<T>
{
    protected val listeners: MutableList<Pair<Project, Pair<String, AChangeListener<T>>>> = mutableListOf()

    fun register(project: Project, key: String, listener: AChangeListener<T>)
    {
        unregister(project, key)
        listeners.add(project to (key to listener))
    }

    fun unregister(project: Project, key: String)
    {
        val foundListener = listeners
            .filter { it.first == project }
            .find { it.second.first == key } ?: return

        listeners.remove(foundListener)
    }

    fun unregisterAll()
    {
        listeners.clear()
    }
}