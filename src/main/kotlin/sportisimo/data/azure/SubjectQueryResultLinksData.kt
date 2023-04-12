package sportisimo.data.azure

data class SubjectQueryResultLinksData(
    val self: LinkData,
    val memberships: LinkData,
    val membershipState: LinkData,
    val storageKey: LinkData,
    val avatar: LinkData?
)