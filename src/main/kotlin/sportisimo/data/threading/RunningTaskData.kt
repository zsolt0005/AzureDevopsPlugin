package sportisimo.data.threading

import java.util.concurrent.Future

data class RunningTaskData<T>(
    var task: Future<T>? = null,
    var cancelled: Boolean = false
)