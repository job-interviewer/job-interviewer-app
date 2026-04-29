package com.interview.server.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class StartInterviewRequest(
    val coverLetterText: String? = null,
    val followUpEnabled: Boolean = true
)

data class QuestionDto(
    val questionId: String,
    val content: String,
    val orderIndex: Int
)

data class StartInterviewResponse(
    val sessionId: String,
    val jobField: String,
    val questions: List<QuestionDto>
)

data class SubmitAnswerRequest(
    val questionId: String,
    val answer: String
)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class SubmitAnswerResponse(
    val needsFollowUp: Boolean,
    val followUpQuestion: QuestionDto?
)

data class CompleteInterviewResponse(
    val sessionId: String,
    val jobField: String,
    val completedAt: String
)

data class ErrorResponse(
    val error: String,
    val message: String
)
