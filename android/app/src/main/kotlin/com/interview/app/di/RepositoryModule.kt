package com.interview.app.di

import com.interview.app.data.repository.InterviewRepositoryImpl
import com.interview.app.domain.repository.InterviewRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds InterviewRepositoryImpl as the implementation to provide for InterviewRepository in Hilt's DI graph.
     *
     * @param impl The concrete InterviewRepositoryImpl instance to be provided when InterviewRepository is requested.
     * @return The bound InterviewRepository interface implemented by the provided instance.
     */
    @Binds
    @Singleton
    abstract fun bindInterviewRepository(impl: InterviewRepositoryImpl): InterviewRepository
}
