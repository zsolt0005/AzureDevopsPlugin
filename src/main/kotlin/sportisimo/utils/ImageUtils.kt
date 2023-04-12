package sportisimo.utils

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.intellij.icons.AllIcons
import com.intellij.util.ui.UIUtil
import sportisimo.data.ui.AvatarIconOptionsData
import java.awt.Image
import java.awt.RenderingHints
import java.awt.color.ColorSpace
import java.awt.geom.Arc2D
import java.awt.image.BufferedImage
import java.awt.image.ColorConvertOp
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon

/**
 * Utils to work with images, resize, reshape or convert to another format.
 *
 * @author Zsolt Döme
 * @since 03.02.2023
 */
object ImageUtils
{
    /** This value specifies how many times will be the image up-scaled, before processing. */
    private const val upScaleValue = 10

    /**
     * Creates a circular clip mask around the given image.
     *
     * @param image  The image to be placed in the circular clip mask.
     * @param options The final image options.
     * @return The circular clip masked image.
     */
    fun circularClipMask(image: BufferedImage, options: AvatarIconOptionsData): Image
    {
        val tempImage: BufferedImage = image.getSubimage(0, 0, image.width, image.height).let {
            BufferedImage(it.colorModel, it.copyData(null), it.isAlphaPremultiplied, null)
        }

        val heightScale: Float = getSizeScale(tempImage.height, options.size)
        val newWidth = (tempImage.width * heightScale).toInt()

        val scaledHeight = options.size * upScaleValue
        val scaledWidth = newWidth * upScaleValue

        if(options.isGrayScale)
        {
            val colorConvertOp = ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null)
            colorConvertOp.filter(tempImage, tempImage)
        }

        val resizedImage = tempImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH)
        val newImage = UIUtil.createImage(null, scaledHeight, scaledHeight, BufferedImage.TYPE_INT_ARGB)
        val graphics = newImage.createGraphics()

        // Enable antialiasing
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Circle clip mask
        graphics.clip(Arc2D.Float(0f, 0f, scaledHeight.toFloat(), scaledHeight.toFloat(), 0f, 360f, Arc2D.PIE))

        // Draw the image at the center of the circular clip mask
        val x = (scaledHeight - resizedImage.getWidth(null)) / 2
        val y = (scaledHeight - resizedImage.getHeight(null)) / 2
        graphics.drawImage(resizedImage, x, y, null)

        // Release resources
        graphics.dispose()

        return newImage.getScaledInstance(options.size, options.size, Image.SCALE_SMOOTH)
    }

    /**
     * Converts a Svg string to an Icon
     *
     * @param svgString Svg string.
     * @param width     Output width.
     * @param height    Output height.
     * @return The converted Svg to an Icon
     */
    fun svgToIcon(svgString: String, width: Int, height: Int): Icon
    {
        if(svgString.isEmpty()) return AllIcons.Actions.Stub

        val inputStream = ByteArrayInputStream(svgString.toByteArray())
        val image = FlatSVGIcon(inputStream).image
        val scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)

        return ImageIcon(scaledImage)
    }

    /**
     * Resizes the given image to the specified width and height.
     *
     * @param icon   The icon to be resized.
     * @param width  Output width.
     * @param height Output height.
     * @return The resized image icon.
     */
    fun resizeIcon(icon: ImageIcon, width: Int, height: Int): ImageIcon
    {
        val image = icon.image
        val scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)

        return ImageIcon(scaledImage)
    }

    fun iconToBufferedImage(icon: Icon): BufferedImage
    {
        val bufferedImage = UIUtil.createImage(null, icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_RGB)

        val graphics = bufferedImage.createGraphics()
        icon.paintIcon(null, graphics, 0, 0)
        graphics.dispose()

        return bufferedImage
    }

    fun bufferedImageToBase64String(image: BufferedImage): String
    {
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        val imageBytes = outputStream.toByteArray()
        return Base64.getEncoder().encodeToString(imageBytes)
    }

    fun base64StringToBufferedImage(base64String: String): BufferedImage {
        val imageBytes = Base64.getDecoder().decode(base64String)
        val inputStream = ByteArrayInputStream(imageBytes)
        return ImageIO.read(inputStream)
    }

    /**
     * Calculates the growth % of the image from the old and the new size.
     *
     * @param oldSize Old size of the image.
     * @param newSize New size of the image.
     * @return The growth percentage of the size.
     */
    private fun getSizeScale(oldSize: Int, newSize: Int): Float = newSize.toFloat() * 100 / oldSize / 100
}