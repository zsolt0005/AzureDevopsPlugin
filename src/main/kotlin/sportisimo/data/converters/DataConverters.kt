package sportisimo.data.converters

import com.google.gson.Gson
import sportisimo.data.*
import sportisimo.data.azure.IdentityData
import sportisimo.data.azure.RepositoryData
import sportisimo.data.azure.RepositoryRefData
import sportisimo.data.cache.GitItemsCacheData

object DataConverters
{
    class ConnectionDataConverter: ADataConverter<ConnectionData>()
    {
        override fun fromString(value: String): ConnectionData
        {
            return Gson().fromJson(value, ConnectionData::class.java)
        }
    }

    class ConnectionsDataConverter: ADataConverter<ConnectionsData>()
    {
        override fun fromString(value: String): ConnectionsData
        {
            return Gson().fromJson(value, ConnectionsData::class.java)
        }
    }

    class GitRepositoryDataConverter: ADataConverter<GitRepositoryData>()
    {
        override fun fromString(value: String): GitRepositoryData
        {
            return Gson().fromJson(value, GitRepositoryData::class.java)
        }
    }

    class IdentityDataConverter: ADataConverter<IdentityData>()
    {
        override fun fromString(value: String): IdentityData
        {
            return Gson().fromJson(value, IdentityData::class.java)
        }
    }

    class NotificationStatesDataConverter: ADataConverter<NotificationStatesData>()
    {
        override fun fromString(value: String): NotificationStatesData
        {
            return Gson().fromJson(value, NotificationStatesData::class.java)
        }
    }

    class ProjectTeamsDataConverter: ADataConverter<ProjectTeamsData>()
    {
        override fun fromString(value: String): ProjectTeamsData
        {
            return Gson().fromJson(value, ProjectTeamsData::class.java)
        }
    }

    class PullRequestsDataConverter: ADataConverter<PullRequestsData>()
    {
        override fun fromString(value: String): PullRequestsData
        {
            return Gson().fromJson(value, PullRequestsData::class.java)
        }
    }

    class PullRequestStateDataConverter: ADataConverter<PullRequestStateData>()
    {
        override fun fromString(value: String): PullRequestStateData
        {
            return Gson().fromJson(value, PullRequestStateData::class.java)
        }
    }

    class RepositoryDataConverter: ADataConverter<RepositoryData>()
    {
        override fun fromString(value: String): RepositoryData
        {
            return Gson().fromJson(value, RepositoryData::class.java)
        }
    }

    class RepositoryRefDataConverter: ADataConverter<RepositoryRefData>()
    {
        override fun fromString(value: String): RepositoryRefData
        {
            return Gson().fromJson(value, RepositoryRefData::class.java)
        }
    }

    class WorkItemTypesDataConverter: ADataConverter<WorkItemTypesData>()
    {
        override fun fromString(value: String): WorkItemTypesData
        {
            return Gson().fromJson(value, WorkItemTypesData::class.java)
        }
    }

    class LastOpenedPullRequestDataConverter: ADataConverter<LastOpenedPullRequestData>()
    {
        override fun fromString(value: String): LastOpenedPullRequestData
        {
            return Gson().fromJson(value, LastOpenedPullRequestData::class.java)
        }
    }

    class GitItemsCacheDataConverter: ADataConverter<GitItemsCacheData>()
    {
        override fun fromString(value: String): GitItemsCacheData
        {
            return Gson().fromJson(value, GitItemsCacheData::class.java)
        }
    }
}