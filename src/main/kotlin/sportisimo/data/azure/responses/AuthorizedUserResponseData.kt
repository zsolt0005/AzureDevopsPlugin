package sportisimo.data.azure.responses

import sportisimo.data.azure.IdentityData

data class AuthorizedUserResponseData(
    val authenticatedUser: IdentityData,
    val authorizedUser: IdentityData,
    val instanceId: String,
    val deploymentId: String,
    val deploymentType: String
)