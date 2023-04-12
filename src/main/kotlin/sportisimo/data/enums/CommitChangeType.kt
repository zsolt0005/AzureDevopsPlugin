package sportisimo.data.enums

enum class CommitChangeType(val value: String)
{
    Add("add"),
    All("all"),
    Branch("branch"),
    Delete("delete"),
    Edit("edit"),
    Encoding("encoding"),
    Lock("lock"),
    Merge("merge"),
    None("none"),
    Property("property"),
    Rename("rename"),
    Rollback("rollback"),
    SourceRename("sourceRename"),
    TargetRename("targetRename"),
    Undelete("undelete")
}