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

    override suspend fun completeInterview(sessionId: String): Result<Unit> {
        return try {
            val response = api.completeInterview(sessionId)
            if (response.isSuccessful) Result.Success(Unit)
            else Result.Error("면접 완료 실패", response.code().toString())
        } catch (e: Exception) {
            Result.Error(e.message ?: "네트워크 오류")
        }
    }

    override suspend fun saveSessionLocally(sessionId: String, jobField: String, messages: List<ChatMessage>) {
        db.interviewDao().insertSession(InterviewSessionEntity(sessionId, jobField))
        db.interviewDao().insertMessages(messages.map {
            ChatMessageEntity(sessionId = sessionId, messageType = it.type.name, content = it.content, questionId = it.questionId)
        })
    }
}
