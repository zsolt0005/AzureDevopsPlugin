package sportisimo.data

data class NotificationStatesData(
    var notificationLevel: Int = 3,
    var testConnection: Boolean = true,
    var toolWindowInitialization: Boolean = true,
    var missingRequiredScope: Boolean = true,
    var missingOptionalScope: Boolean = true,
    var localGit: Boolean = true
)