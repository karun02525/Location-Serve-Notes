package `in`.antef.network.di

import android.content.Context
import com.amazonaws.regions.Regions
import `in`.antef.network.data.repository.FileRepositoryImpl
import `in`.antef.network.data.source.S3DataSource
import `in`.antef.network.data.source.S3DataSourceImpl
import `in`.antef.network.domain.repository.FileRepository
import `in`.antef.network.domain.usecase.UploadFileUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Koin module for the file upload feature.
 */
val fileUploadModule = module {
    // S3 Configuration using Cognito
    single<S3DataSource> {
        S3DataSourceImpl(
            context = get<Context>(),
            identityPoolId = "ap-south-1:d878be59-7280-41bb-bd70-1df4b4acacd7", // Use from secure config
            bucketName = "geonote-dev-mobile", // Use from secure config
            region = Regions.AP_SOUTH_1
        )
    }

    // Repository
    single<FileRepository> { FileRepositoryImpl(s3DataSource = get()) }

    // Use Case
    factoryOf(::UploadFileUseCase)

    // ViewModel
   // viewModelOf(::FileUploadViewModel)
}