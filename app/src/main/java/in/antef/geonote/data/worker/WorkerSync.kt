package `in`.antef.geonote.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WorkerSync(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
    private val syncData: SyncData by inject()
    override suspend fun doWork(): Result {
        syncData.doWorkSync()
        return Result.success()
    }
}