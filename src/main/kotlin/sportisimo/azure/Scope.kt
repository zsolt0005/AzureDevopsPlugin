package sportisimo.azure

enum class Scope
{
    Projects,
    Teams,
    GitRepositories,
    GitBranchRefs,
    PullRequests,
    PullRequestWorkItems,
    PullRequestCommits,
    PullRequestThreads,
    PullRequestIterations,
    PullRequestIterationChanges,
    BranchCommits,
    CommitChanges,
    GitItems,
    CreatePullRequest,
    UpdatePullRequest,
    DeletePullRequestThreadComment,
    UpdatePullRequestThreadComment,
    CreatePullRequestThreadComment,
    CreatePullRequestThread,
    UpdatePullRequestThread,
    UpdatePullRequestReviewer,
    SubjectQuery,
    SubjectAvatar,
    AvatarIcons,
    WorkItemTypeIcons,
    WorkItems,
    BuildRuns,
    NoScope
}

enum class ScopeValue(val value: String)
{
    Read("Read"),
    ReadAndWrite("Read & Write")
}