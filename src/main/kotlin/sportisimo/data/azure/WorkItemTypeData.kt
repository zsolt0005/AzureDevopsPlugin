package sportisimo.data.azure

data class WorkItemTypeData(
    val name: String,
    val referenceName: String,
    val description: String,
    val color: String,
    val icon: WorkItemIconData,
    val isDisabled: Boolean,
)