package `in`.antef.geonote.di

import CheckNetworkConnectivity
import `in`.antef.geonote.data.service.FileUploadManager
import `in`.antef.geonote.data.service.LocationService
import `in`.antef.geonote.data.worker.SyncData
import `in`.antef.geonote.domain.usecase.InsertMediaUseCase
import `in`.antef.geonote.domain.usecase.UploadCoordinateFileUseCase
import `in`.antef.geonote.ui.viewmodel.CoordinatesViewModel
import `in`.antef.geonote.ui.viewmodel.FileUploadViewModel
import `in`.antef.geonote.ui.viewmodel.HomeViewModel
import `in`.antef.geonote.ui.viewmodel.LocationViewModel
import `in`.antef.geonote.ui.viewmodel.RecordingViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // Services
    single { LocationService(androidContext()) }
    single { FileUploadManager(androidContext()) }

    // Use Cases
    factoryOf(::InsertMediaUseCase)
    factory { UploadCoordinateFileUseCase(get(), get()) }
    factory { SyncData(get(), get()) }

    // ViewModels
    viewModelOf(::HomeViewModel)
    viewModelOf(::LocationViewModel)
    viewModel {
        FileUploadViewModel(
            get(),
            get(),
            get(),
            get()
        )
    }
    viewModelOf(::CoordinatesViewModel)
    viewModel { RecordingViewModel(get(), get(), get()) }
    single { CheckNetworkConnectivity(get()) }
}
