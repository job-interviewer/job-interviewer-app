package com.interview.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.interview.app.data.local.dao.InterviewDao
import com.interview.app.data.local.entity.ChatMessageEntity
import com.interview.app.data.local.entity.InterviewSessionEntity

@Database(
    entities = [InterviewSessionEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class InterviewDatabase : RoomDatabase() {
    /**
 * Provides the DAO for accessing interview-related entities.
 *
 * @return An [InterviewDao] for performing CRUD operations on interview sessions and chat messages.
 */
abstract fun interviewDao(): InterviewDao
}
