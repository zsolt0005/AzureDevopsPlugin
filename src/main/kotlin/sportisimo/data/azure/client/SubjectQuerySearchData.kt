package sportisimo.data.azure.client

data class SubjectQuerySearchData(
    val subjectKind: List<String>,
    val query: String
)

enum class SubjectQuerySearchSubjectKind(val value: String)
{
    User("User"),
    Group("Group")
}