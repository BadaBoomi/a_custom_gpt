package com.badaboomi.acustomgpt.di

import com.badaboomi.acustomgpt.data.remote.OpenAiApiService
import com.badaboomi.acustomgpt.data.security.EncryptedPrefsManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(encryptedPrefsManager: EncryptedPrefsManager): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val apiKey = encryptedPrefsManager.getApiKey() ?: ""
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("OpenAI-Beta", "assistants=v2")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideOpenAiApiService(retrofit: Retrofit): OpenAiApiService =
        retrofit.create(OpenAiApiService::class.java)
}
