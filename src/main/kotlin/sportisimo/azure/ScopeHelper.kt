package sportisimo.azure

import com.intellij.notification.NotificationType
import sportisimo.data.ScopeData
import sportisimo.states.AppSettingsState
import sportisimo.utils.NotificationUtils

object ScopeHelper
{
    private val settings = AppSettingsState.getInstance()

    private val scope: List<ScopeData> = listOf(
        ScopeData(Scope.CreatePullRequest, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.UpdatePullRequest, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.UpdatePullRequestReviewer, true, "Code", ScopeValue.ReadAndWrite.value),

        ScopeData(Scope.DeletePullRequestThreadComment, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.DeletePullRequestThreadComment, true, "Pull Request Threads", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.UpdatePullRequestThreadComment, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.UpdatePullRequestThreadComment, true, "Pull Request Threads", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.CreatePullRequestThreadComment, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.CreatePullRequestThreadComment, true, "Pull Request Threads", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.CreatePullRequestThread, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.CreatePullRequestThread, true, "Pull Request Threads", ScopeValue.ReadAndWrite.value),

        ScopeData(Scope.UpdatePullRequestThread, true, "Code", ScopeValue.ReadAndWrite.value),
        ScopeData(Scope.UpdatePullRequestThread, true, "Pull Request Threads", ScopeValue.ReadAndWrite.value),

        ScopeData(Scope.Projects, true,"Project and Team", ScopeValue.Read.value),
        ScopeData(Scope.Teams, true,"Project and Team", ScopeValue.Read.value),
        ScopeData(Scope.GitRepositories, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.GitBranchRefs, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequests, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequestWorkItems, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequestCommits, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequestThreads, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequestIterations, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.PullRequestIterationChanges, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.BranchCommits, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.CommitChanges, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.GitItems, true, "Code", ScopeValue.Read.value),
        ScopeData(Scope.WorkItems, true, "Work Items", ScopeValue.Read.value),
        ScopeData(Scope.SubjectQuery, true, "Graph", ScopeValue.Read.value),
        ScopeData(Scope.BuildRuns, true, "Build", ScopeValue.Read.value),

        ScopeData(Scope.WorkItemTypeIcons, false, "Work Items", ScopeValue.Read.value),
        ScopeData(Scope.AvatarIcons, false, "Identity", ScopeValue.Read.value),
        ScopeData(Scope.SubjectAvatar, false, "Graph", ScopeValue.Read.value)
    )

    fun getRequiredScopesTooltip(): String
    {
        val distinctScopes = scope.distinctBy { it.scopeName }

        return """
            <b>Required scopes</b>
            ${getScopesAsHtmlTable(distinctScopes, true)}
            <br>
            <b>Optional scopes</b>
            ${getScopesAsHtmlTable(distinctScopes, false)}
        """.trimIndent()
    }

    fun getScopeByName(searchedScope: Scope): ScopeData?
    {
        return scope.find {
            it.scopeType == searchedScope
        }
    }

    fun notifyMissingScope(
        missingScopeData: ScopeData?,
        message: String = "",
        notificationType: NotificationType = NotificationType.WARNING
    )
    {
        var notificationBody = ""

        if(message.isNotEmpty())
        {
            notificationBody += "<b>Response from the Azure API: $message</b>"
        }

        val scopeType =
            if(missingScopeData != null)
                if(missingScopeData.isRequired) " a required" else " an optional"
            else ""

        notificationBody += "<br><br><b>Your connection token is missing$scopeType scope!</b><br>"

        notificationBody += if(missingScopeData != null)
        {
            """
                <table>
                <tr>
                    <td>${missingScopeData.scopeName}</td>
                    <td>${missingScopeData.scopeValue}</td>
                </tr>
            </table>
            """
        }
        else
        {
            "We are not able to identify the missing scope!" +
                    if(message.isNotEmpty()) "<br>The Azure API Response might be helpful"
                    else ""
        }

        NotificationUtils.notify(
            "Missing $scopeType scope",
            notificationBody,
            notificationType,
            true,
            isAllowed = if(missingScopeData?.isRequired == true) settings.notificationStates.missingRequiredScope else settings.notificationStates.missingOptionalScope
        )
    }

    //** Helpers **//

    /**
     * @param scopes
     * @param onlyRequired **If true**, includes only required scopes. **If false**, includes only optional scopes. **If null** includes both
     * @return constructed HTML string
     */
    private fun getScopesAsHtmlTable(scopes: List<ScopeData>, onlyRequired: Boolean? = null): String
    {
        var tableHtml = "<table> "

        scopes.forEach {
            // Skip not required scopes
            if(
                (onlyRequired == true && !it.isRequired)
             || (onlyRequired == false && it.isRequired)
            )
            {
                return@forEach
            }

            tableHtml += """
                <tr>
                    <td style='color: #8c9496;'>${it.scopeName}</td>
                    <td style='color: #8c9496;'><b>${it.scopeValue}</b></td>
                </tr>
            """.trimIndent()
        }

        tableHtml += "</table>"

        return tableHtml
    }
}