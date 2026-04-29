package com.interview.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.interview.app.data.local.entity.ChatMessageEntity
import com.interview.app.data.local.entity.InterviewSessionEntity

@Dao
interface InterviewDao {
    /**
     * Inserts or replaces an interview session in the local database.
     *
     * @param session The interview session entity to persist; if a row with the same primary key exists it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InterviewSessionEntity)

    /**
     * Inserts the given chat messages into the messages table, replacing any existing rows that conflict on primary or unique keys.
     *
     * @param messages The list of chat messages to persist.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    /**
     * Retrieve all interview sessions ordered by completion time, newest first.
     *
     * @return A list of InterviewSessionEntity objects ordered by `completedAt` descending.
     */
    @Query("SELECT * FROM interview_sessions ORDER BY completedAt DESC")
    suspend fun getAllSessions(): List<InterviewSessionEntity>
}
