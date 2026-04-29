package com.interview.server.model

import java.time.Instant
import java.util.UUID

data class InterviewQuestion(
    val questionId: String = UUID.randomUUID().toString(),
    val content: String,
    val category: String,
    var answer: String? = null,
    var followUpQuestion: InterviewQuestion? = null,
    val isFollowUp: Boolean = false
)

enum class SessionStatus { IN_PROGRESS, COMPLETED }

data class InterviewSession(
    val sessionId: String = UUID.randomUUID().toString(),
    val jobField: String,
    val coverLetterText: String,
    val followUpEnabled: Boolean,
    val questions: MutableList<InterviewQuestion> = mutableListOf(),
    var status: SessionStatus = SessionStatus.IN_PROGRESS,
    val createdAt: Instant = Instant.now(),
    var completedAt: Instant? = null
)
