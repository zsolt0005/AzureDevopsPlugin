package sportisimo.utils

import javax.swing.JOptionPane

object ConfirmDialogUtils
{
    fun yesNo(title: String, message: String): Int
    {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION)
    }

    fun confirm(title: String, message: String, cb: () -> Unit)
    {
        val confirm = yesNo(title, message)
        if (confirm != JOptionPane.YES_OPTION) return

        cb()
    }
}