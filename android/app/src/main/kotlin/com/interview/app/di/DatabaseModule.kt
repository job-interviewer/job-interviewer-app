package com.interview.app.di

import android.content.Context
import androidx.room.Room
import com.interview.app.data.local.InterviewDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InterviewDatabase =
        Room.databaseBuilder(context, InterviewDatabase::class.java, "interview.db").build()
}
