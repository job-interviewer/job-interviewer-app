package com.interview.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey val sessionId: String,
    val jobField: String,
    val completedAt: Long = System.currentTimeMillis()
)
