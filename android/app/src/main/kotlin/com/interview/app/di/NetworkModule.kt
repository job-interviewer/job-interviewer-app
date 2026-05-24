package com.interview.app.di

import com.interview.app.BuildConfig
import com.interview.app.data.remote.api.InterviewApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
         * Creates a Moshi instance configured for Kotlin data classes.
         *
         * Registers `KotlinJsonAdapterFactory` so Moshi can handle Kotlin-specific constructs (data classes, non-null types).
         *
         * @return A `Moshi` instance with `KotlinJsonAdapterFactory` registered.
         */
        @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    /**
         * Provides a singleton OkHttpClient configured with an HTTP logging interceptor that logs request and response bodies.
         *
         * @return An OkHttpClient instance with HTTP logging set to `BODY`.
         */
        @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .build()

    /**
             * Creates a Retrofit instance configured for the application's API.
             *
             * @param okHttpClient The HTTP client to use for network requests.
             * @param moshi The Moshi instance used to serialize and deserialize JSON.
             * @return A Retrofit instance configured with the application's base URL, the provided OkHttpClient, and a Moshi converter.
             */
            @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
         * Creates an implementation of InterviewApi using the provided Retrofit instance.
         *
         * @param retrofit Retrofit instance used to create the API implementation.
         * @return An InterviewApi implementation backed by the given Retrofit client.
         */
        @Provides
    @Singleton
    fun provideInterviewApi(retrofit: Retrofit): InterviewApi =
        retrofit.create(InterviewApi::class.java)
}
