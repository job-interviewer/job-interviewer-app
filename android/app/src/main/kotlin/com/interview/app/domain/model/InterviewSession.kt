package com.interview.app.domain.model

data class InterviewSession(
    val sessionId: String,
    val jobField: String,
    val questions: List<Question>
)
