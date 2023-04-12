package sportisimo.data.azure

import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import sportisimo.data.ui.AvatarIconOptionsData
import sportisimo.threading.FutureNotice
import sportisimo.utils.ImageUtils
import java.util.concurrent.Callable
import java.util.concurrent.Future
import javax.swing.Icon
import javax.swing.ImageIcon

abstract class AAsyncImageData
{
    open var asyncImageIcon: Future<Icon>? = null

    fun getImageIconAsync(options: AvatarIconOptionsData? = null): Future<Icon> = ApplicationManager.getApplication().executeOnPooledThread(Callable {
        return@Callable getImageIcon(options)
    })

    fun getImageIcon(options: AvatarIconOptionsData? = null): Icon
    {
        if (asyncImageIcon == null) return AllIcons.Actions.Stub

        val icon = FutureNotice(asyncImageIcon!!).awaitCompletionAndGetResult()!!

        if(options != null)
        {
            val bufferedImage = ImageUtils.iconToBufferedImage(icon)
            return ImageIcon(ImageUtils.circularClipMask(bufferedImage, options))
        }

        return icon
    }
}