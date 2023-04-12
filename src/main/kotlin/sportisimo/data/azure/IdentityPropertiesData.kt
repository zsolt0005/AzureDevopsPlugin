package sportisimo.data.azure

import com.google.gson.annotations.SerializedName

data class IdentityPropertiesData(
    @SerializedName("Account") val account: IdentityPropertiesAccountData
)

data class IdentityPropertiesAccountData(
    @SerializedName("${"$"}type") val type: String,
    @SerializedName("${"$"}value") val value: String
)