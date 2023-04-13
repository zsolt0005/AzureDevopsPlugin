package sportisimo.data.azure

import java.util.concurrent.Future
import javax.swing.Icon

data class IdentityData(
    val id: String,
    val descriptor: String,
    val subjectDescriptor: String,
    val providerDisplayName: String,
    val isActive: Boolean,
    val properties: IdentityPropertiesData,
    val resourceVersion: Int,
    val metaTypeId: Int
): AAsyncImageData()
{
    @Transient var asyncImageIcon: Future<Icon>? = null

    override fun getAsyncIcon(): Future<Icon>? = asyncImageIcon
}