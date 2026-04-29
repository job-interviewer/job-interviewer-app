package com.interview.app.data.repository

import com.interview.app.data.local.InterviewDatabase
import com.interview.app.data.local.entity.ChatMessageEntity
import com.interview.app.data.local.entity.InterviewSessionEntity
import com.interview.app.data.remote.api.InterviewApi
import com.interview.app.data.remote.dto.StartInterviewRequest
import com.interview.app.data.remote.dto.SubmitAnswerRequest
import com.interview.app.domain.model.AnswerResult
import com.interview.app.domain.model.ChatMessage
import com.interview.app.domain.model.CoverLetter
import com.interview.app.domain.model.CoverLetterType
import com.interview.app.domain.model.InterviewSession
import com.interview.app.domain.model.Question
import com.interview.app.domain.model.Result
import com.interview.app.domain.repository.InterviewRepository
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(
    private val api: InterviewApi,
    private val db: InterviewDatabase
) : InterviewRepository {

    /**
     * Starts an interview session using the provided cover letter and follow-up preference.
     *
     * @param coverLetter The cover letter to start the interview with; for TEXT this uses the embedded text, for PDF/DOCX this uses the file at `coverLetter.filePath`.
     * @param followUpEnabled Whether follow-up questions are enabled for the session.
     * @return `Result.Success` with an `InterviewSession` when the remote API responds successfully; `Result.Error` containing an error message and, when applicable, the HTTP status code otherwise. On exceptions returns `Result.Error` with the exception message or `"네트워크 오류"` if the message is null.
     */
    override suspend fun startInterview(coverLetter: CoverLetter, followUpEnabled: Boolean): Result<InterviewSession> {
        return try {
            val response = when (coverLetter.fileType) {
                CoverLetterType.TEXT -> api.startInterview(
                    StartInterviewRequest(coverLetter.text, followUpEnabled)
                )
                CoverLetterType.PDF, CoverLetterType.DOCX -> {
                    val file = File(coverLetter.filePath!!)
                    val mimeType = if (coverLetter.fileType == CoverLetterType.PDF) "application/pdf"
                    else "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    val filePart = MultipartBody.Part.createFormData(
                        "file", file.name, file.asRequestBody(mimeType.toMediaType())
                    )
                    val followUpPart = followUpEnabled.toString().toRequestBody("text/plain".toMediaType())
                    api.startInterviewWithFile(filePart, followUpPart)
                }
            }
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.Success(InterviewSession(
                    sessionId = body.sessionId,
                    jobField = body.jobField,
                    questions = body.questions.map { Question(it.questionId, it.content, it.orderIndex) }
                ))
            } else {
                Result.Error("면접 시작 실패", response.code().toString())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류")
        }
    }

    /**
     * Submits an answer for a question in an interview session and returns the evaluation result.
     *
     * @param sessionId The identifier of the interview session.
     * @param questionId The identifier of the question being answered.
     * @param answer The answer text to submit.
     * @return A Result containing an AnswerResult when the API accepts the answer; otherwise a Result.Error
     *         containing an HTTP status code on non-successful responses or the exception message. If an
     *         exception has no message, the error message will be "네트워크 오류".
     */
    override suspend fun submitAnswer(sessionId: String, questionId: String, answer: String): Result<AnswerResult> {
        return try {
            val response = api.submitAnswer(sessionId, SubmitAnswerRequest(questionId, answer))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                Result.Success(AnswerResult(
                    needsFollowUp = body.needsFollowUp,
                    followUpQuestion = body.followUpQuestion?.let {
                        Question(it.questionId, it.content, it.orderIndex)
                    }
                ))
            } else {
                Result.Error("답변 제출 실패", response.code().toString())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류")
        }
    }

    /**
     * Marks the interview identified by the given sessionId as complete on the remote service.
     *
     * @param sessionId The interview session identifier to complete.
     * @return `Result.Success(Unit)` if the completion request succeeded, `Result.Error` containing an error message (and HTTP status code when available) otherwise.
     */
    override suspend fun completeInterview(sessionId: String): Result<Unit> {
        return try {
            val response = api.completeInterview(sessionId)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("면접 완료 실패", response.code().toString())
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류")
        }
    }

    /**
     * Persists an interview session and its chat messages into the local database.
     *
     * @param sessionId Identifier of the interview session to save.
     * @param jobField Job field associated with the session.
     * @param messages Chat messages to store; each message is converted into a ChatMessageEntity with `messageType` set to the message's type name, `content`, and associated `questionId`.
     */
    override suspend fun saveSessionLocally(sessionId: String, jobField: String, messages: List<ChatMessage>) {
        db.interviewDao().insertSession(InterviewSessionEntity(sessionId, jobField))
        db.interviewDao().insertMessages(messages.map {
            ChatMessageEntity(sessionId = sessionId, messageType = it.type.name, content = it.content, questionId = it.questionId)
        })
    }
}
