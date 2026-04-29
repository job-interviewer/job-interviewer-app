package com.interview.app.domain.repository

import com.interview.app.domain.model.AnswerResult
import com.interview.app.domain.model.ChatMessage
import com.interview.app.domain.model.CoverLetter
import com.interview.app.domain.model.InterviewSession
import com.interview.app.domain.model.Result

interface InterviewRepository {
    /**
 * Starts a new interview session using the provided cover letter.
 *
 * @param coverLetter The applicant's cover letter used to initialize the interview.
 * @param followUpEnabled Whether follow-up questions should be enabled for the session.
 * @return A `Result` containing the created `InterviewSession` on success, or a failure result on error.
 */
suspend fun startInterview(coverLetter: CoverLetter, followUpEnabled: Boolean): Result<InterviewSession>
    /**
 * Submits an answer for a specific question within an interview session.
 *
 * @param sessionId Identifier of the interview session.
 * @param questionId Identifier of the question being answered.
 * @param answer The respondent's answer text.
 * @return A Result wrapping an AnswerResult containing the submission outcome and any follow-up information.
suspend fun submitAnswer(sessionId: String, questionId: String, answer: String): Result<AnswerResult>
    /**
 * Marks the interview identified by the given sessionId as complete.
 *
 * @param sessionId The identifier of the interview session to complete.
 * @return A `Result` containing `Unit` on success, or an error describing the failure.
 */
suspend fun completeInterview(sessionId: String): Result<Unit>
    /**
 * Persists an interview session to local storage.
 *
 * @param sessionId The unique identifier of the interview session.
 * @param jobField The job field or role associated with the session (e.g., "software engineer").
 * @param messages The sequence of chat messages exchanged in the session to be saved.
 */
suspend fun saveSessionLocally(sessionId: String, jobField: String, messages: List<ChatMessage>)
}
