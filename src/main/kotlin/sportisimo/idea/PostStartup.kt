package sportisimo.idea

import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class PostStartup: EditorFactoryListener
{
    override fun editorCreated(event: EditorFactoryEvent)
    {
        super.editorCreated(event)

        EditorCommentsManager.opened(event)
    }

    override fun editorReleased(event: EditorFactoryEvent)
    {
        super.editorReleased(event)

        EditorCommentsManager.closed(event)
    }
}