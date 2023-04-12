package sportisimo.services

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import sportisimo.azure.Connection
import sportisimo.data.azure.*
import sportisimo.data.azure.client.NewPullRequestThreadCommentData
import sportisimo.data.azure.client.NewThreadData
import sportisimo.data.azure.client.UpdatePullRequestThreadData
import sportisimo.exceptions.PullRequestException
import sportisimo.threading.ThreadingManager

class PullRequestThreadService
{
    fun deleteComment(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData
    ) = ThreadingManager.executeOnPooledThread {
        connection.gitClient.deletePullRequestThreadComment(
            azureProject,
            pullRequest.repository,
            pullRequest,
            thread,
            comment
        )

        reloadComments(project)
    }

    fun addLikeToComment(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData,
        currentUser: IdentityData
    ) = ThreadingManager.executeOnPooledThread {
        val alreadyLiked = comment.usersLiked?.any { it.id == currentUser.id } ?: false

        if(alreadyLiked)
        {
            connection.gitClient.deletePullRequestThreadCommentLike(
                azureProject,
                pullRequest.repository,
                pullRequest,
                thread,
                comment
            )
        }
        else
        {
            connection.gitClient.addPullRequestThreadCommentLike(
                azureProject,
                pullRequest.repository,
                pullRequest,
                thread,
                comment
            )
        }

        reloadComments(project)
    }

    fun updateThreadStatus(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        newStatus: String
    ) = ThreadingManager.executeOnPooledThread {
        val updateData = UpdatePullRequestThreadData(newStatus)

        connection.gitClient.updatePullRequestThread(
            azureProject,
            pullRequest.repository,
            pullRequest,
            thread,
            updateData
        )

        reloadComments(project)
    }

    fun updateThreadStatus(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        newStatus: PullRequestThreadStatus
    ) = updateThreadStatus(project, connection, azureProject, pullRequest, thread, newStatus.value)

    fun addThreadComment(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        content: String
    ) = ThreadingManager.executeOnPooledThread {
        val contentData = NewPullRequestThreadCommentData(content)

        connection.gitClient.addPullRequestThreadComment(
            azureProject,
            pullRequest.repository,
            pullRequest,
            thread,
            contentData
        )

        reloadComments(project)
    }

    fun createThread(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        newThread: NewThreadData
    ): PullRequestThreadData
    {
        val service = project.service<DataProviderService>()

        val createdThread = connection.gitClient.createPullRequestThread(
            azureProject,
            pullRequest.repository,
            pullRequest,
            newThread
        ) ?: throw PullRequestException("Failed to create a new thread")

        createdThread.comments.forEach { comment ->
            comment.author.asyncImageIcon = service.getAvatarAsync(comment.author.descriptor, "large").task!!
        }

        reloadComments(project)
        return createdThread
    }

    fun updateThreadComment(
        project: Project,
        connection: Connection,
        azureProject: ProjectData,
        pullRequest: PullRequestData,
        thread: PullRequestThreadData,
        comment: PullRequestThreadCommentData,
        content: String
    ) = ThreadingManager.executeOnPooledThread {
        val contentData = NewPullRequestThreadCommentData(content)

        connection.gitClient.updatePullRequestThreadComment(
            azureProject,
            pullRequest.repository,
            pullRequest,
            thread,
            comment,
            contentData
        )

        reloadComments(project)
    }

    private fun reloadComments(project: Project)
    {
        val service = project.service<DataProviderService>()
        val lastIteration = service.getPullRequestIterations().last()
        service.getPullRequestThreadsAsync(lastIteration)
    }
}