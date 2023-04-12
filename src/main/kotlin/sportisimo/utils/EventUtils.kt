package sportisimo.utils

import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Simplifies the use of events.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object EventUtils
{
    object DocumentEvents
    {
        /**
         * For each DocumentListener event, calls the given function.
         *
         * @param callback The function to be run on when any of the events are fired.
         * @return The DocumentListener.
         */
        fun onAllDocumentEvent(callback: (e: DocumentEvent) -> Unit) = object : DocumentListener
        {
            override fun changedUpdate(e: DocumentEvent) = callback(e)
            override fun removeUpdate(e: DocumentEvent) = callback(e)
            override fun insertUpdate(e: DocumentEvent) = callback(e)
        }
    }

    object MouseEvents
    {
        /**
         * Calls the given event for the matching MouseListener event. This replaces the need of creating a new object that overrides the
         * MouseListener, and forces to override all the events of it.
         *
         * @param onMousePressed
         * @param onMouseReleased
         * @param onMouseEntered
         * @param onMouseExited
         * @param onMouseClicked
         * @return The MouseListener.
         */
        fun onAnyMouseEvents(
            onMousePressed: ((e: MouseEvent?) -> Unit)? = null,
            onMouseReleased: ((e: MouseEvent?) -> Unit)? = null,
            onMouseEntered: ((e: MouseEvent?) -> Unit)? = null,
            onMouseExited: ((e: MouseEvent?) -> Unit)? = null,
            onMouseClicked: ((e: MouseEvent?) -> Unit)? = null
        ) = object: MouseListener
        {
            override fun mousePressed(e: MouseEvent?) { onMousePressed?.let { it(e) } }
            override fun mouseReleased(e: MouseEvent?) { onMouseReleased?.let { it(e) } }
            override fun mouseEntered(e: MouseEvent?) { onMouseEntered?.let { it(e) } }
            override fun mouseExited(e: MouseEvent?) { onMouseExited?.let { it(e) } }
            override fun mouseClicked(e: MouseEvent?) { onMouseClicked?.let { it(e) } }
        }
    }
}