package ovo.sypw.autoglm4android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ovo.sypw.autoglm4android.domain.repository.AppResolver
import ovo.sypw.autoglm4android.domain.repository.DeviceRepository
import ovo.sypw.autoglm4android.domain.repository.ScreenshotRepository
import ovo.sypw.autoglm4android.service.AppResolverImpl
import ovo.sypw.autoglm4android.service.DeviceExecutor
import ovo.sypw.autoglm4android.service.ScreenshotServiceImpl
import ovo.sypw.autoglm4android.service.ShizukuService
import ovo.sypw.autoglm4android.util.HumanizedSwipeGenerator
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DeviceModule {

    @Provides
    @Singleton
    fun provideShizukuService(): ShizukuService {
        return ShizukuService()
    }

    @Provides
    @Singleton
    fun provideDeviceExecutor(shizukuService: ShizukuService): DeviceExecutor {
        return DeviceExecutor(shizukuService)
    }

    @Provides
    @Singleton
    fun provideHumanizedSwipeGenerator(): HumanizedSwipeGenerator {
        return HumanizedSwipeGenerator()
    }

    @Provides
    @Singleton
    fun provideDeviceRepository(
        deviceExecutor: DeviceExecutor,
        swipeGenerator: HumanizedSwipeGenerator
    ): DeviceRepository {
        return ovo.sypw.autoglm4android.data.repository.DeviceRepositoryImpl(
            deviceExecutor, swipeGenerator
        )
    }

    @Provides
    @Singleton
    fun provideScreenshotRepository(
        shizukuService: ShizukuService
    ): ScreenshotRepository {
        return ScreenshotServiceImpl(shizukuService)
    }

    @Provides
    @Singleton
    fun provideAppResolver(): AppResolver {
        return AppResolverImpl()
    }
}
