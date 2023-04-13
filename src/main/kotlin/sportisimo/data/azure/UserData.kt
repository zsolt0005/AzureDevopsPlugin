package sportisimo.data.azure

import com.google.gson.annotations.SerializedName
import java.util.concurrent.Future
import javax.swing.Icon

data class UserData(
    val displayName: String,
    val url: String,
    val id: String,
    val uniqueName: String,
    val imageUrl: String,
    val descriptor: String,
    @SerializedName("_links") val links: CreatorLinksData
): AAsyncImageData()
{
    @Transient var asyncImageIcon: Future<Icon>? = null

    override fun getAsyncIcon(): Future<Icon>? = asyncImageIcon
}