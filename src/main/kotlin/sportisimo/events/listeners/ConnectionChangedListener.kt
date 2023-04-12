package sportisimo.events.listeners

import com.intellij.openapi.project.Project

class ConnectionChangedListener(project: Project, private val callback: (Any?) -> Unit): AChangeListener<Any>(project)
{
    override fun onChange(result: Any?)
    {
        runCallback(callback, result)
    }
}