package `in`.antef.database.di


import `in`.antef.database.AppDatabase
import `in`.antef.database.repositories.GeoNoteRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.create(androidContext()) }
    single { get<AppDatabase>().projectDao() }
    single { get<AppDatabase>().coordinateDao() }
    single { get<AppDatabase>().photoDao() }
    single { GeoNoteRepository(get(), get(), get()) }
}