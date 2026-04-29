package com.interview.app.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StartInterviewRequest(
    @Json(name = "coverLetterText") val coverLetterText: String?,
    @Json(name = "followUpEnabled") val followUpEnabled: Boolean
)

@JsonClass(generateAdapter = true)
data class QuestionDto(
    @Json(name = "questionId") val questionId: String,
    @Json(name = "content") val content: String,
    @Json(name = "orderIndex") val orderIndex: Int
)

@JsonClass(generateAdapter = true)
data class StartInterviewResponse(
    @Json(name = "sessionId") val sessionId: String,
    @Json(name = "jobField") val jobField: String,
    @Json(name = "questions") val questions: List<QuestionDto>
)

@JsonClass(generateAdapter = true)
data class SubmitAnswerRequest(
    @Json(name = "questionId") val questionId: String,
    @Json(name = "answer") val answer: String
)

@JsonClass(generateAdapter = true)
data class SubmitAnswerResponse(
    @Json(name = "needsFollowUp") val needsFollowUp: Boolean,
    @Json(name = "followUpQuestion") val followUpQuestion: QuestionDto?
)

@JsonClass(generateAdapter = true)
data class CompleteInterviewResponse(
    @Json(name = "sessionId") val sessionId: String,
    @Json(name = "jobField") val jobField: String,
    @Json(name = "completedAt") val completedAt: String
)

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "error") val error: String,
    @Json(name = "message") val message: String
)
