package sportisimo.data.azure

import com.google.gson.annotations.SerializedName
import java.util.concurrent.Future
import javax.swing.Icon

data class SubjectQueryResultData(
    val subjectKind: String,
    val description: String? = null,
    val metaType: String? = null,
    val directoryAlias: String? = null,
    val domain: String,
    val principalName: String,
    val mailAddress: String,
    val origin: String,
    val originId: String,
    val displayName: String,
    @SerializedName("_links") val links: SubjectQueryResultLinksData? = null,
    val url: String,
    val descriptor: String,
    var avatar: Future<Icon>? = null,
    var isRequired: Boolean = false
)