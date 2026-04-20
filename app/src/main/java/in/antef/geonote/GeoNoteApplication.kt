package `in`.antef.geonote

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import `in`.antef.database.di.databaseModule
import `in`.antef.geonote.data.worker.WorkerSync
import `in`.antef.geonote.di.appModule
import `in`.antef.network.di.fileUploadModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class GeoNoteApplication : Application(), Configuration.Provider, KoinComponent {


    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase Crashlytics
        //  FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)


        startKoin {
            androidLogger()
            androidContext(this@GeoNoteApplication)
            modules(
                listOf(
                    appModule,
                    databaseModule,
                    fileUploadModule
                )
            )
        }

/*        CoroutineScope(Dispatchers.Default).launch {
            uploadCoordinateFileUseCase.networkStatusFlow().collect { isConnected ->
                if (isConnected) {
                    println("Connected..........")
                    syncData.doWorkSync()
                }else{
                    println("Connected...Not.......")
                }
            }
        }*/

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<WorkerSync>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(
                "MyPeriodicWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}