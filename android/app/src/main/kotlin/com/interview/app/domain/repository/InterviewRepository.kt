package com.interview.app.domain.repository

import com.interview.app.domain.model.AnswerResult
import com.interview.app.domain.model.ChatMessage
import com.interview.app.domain.model.CoverLetter
import com.interview.app.domain.model.InterviewSession
import com.interview.app.domain.model.Result

interface InterviewRepository {
    suspend fun startInterview(coverLetter: CoverLetter, followUpEnabled: Boolean): Result<InterviewSession>
    suspend fun submitAnswer(sessionId: String, questionId: String, answer: String): Result<AnswerResult>
    suspend fun completeInterview(sessionId: String): Result<Unit>
    suspend fun saveSessionLocally(sessionId: String, jobField: String, messages: List<ChatMessage>)
}
