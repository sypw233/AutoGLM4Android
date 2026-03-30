package ovo.sypw.autoglm4android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import ovo.sypw.autoglm4android.data.remote.api.ModelApi
import ovo.sypw.autoglm4android.domain.model.ModelConfig
import ovo.sypw.autoglm4android.domain.repository.ModelRepository
import ovo.sypw.autoglm4android.data.remote.impl.ModelClientImpl
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideModelApi(
        okHttpClient: OkHttpClient,
        json: Json
    ): ModelApi {
        return Retrofit.Builder()
            .baseUrl("https://open.bigmodel.cn/api/paas/v4/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ModelApi::class.java)
    }

    @Provides
    @Singleton
    fun provideModelRepository(
        api: ModelApi,
        json: Json
    ): ModelRepository {
        return ModelClientImpl(api, json)
    }
}
