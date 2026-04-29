package com.interview.server.service

import com.interview.server.dto.SubmitAnswerResponse
import com.interview.server.dto.QuestionDto
import com.interview.server.dto.CompleteInterviewResponse
import com.interview.server.model.InterviewQuestion
import com.interview.server.model.InterviewSession
import com.interview.server.model.SessionStatus
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

@Service
class InterviewService(
    private val sessionStore: SessionStore,
    private val textExtractorService: TextExtractorService,
    private val jobDetectionService: JobDetectionService,
    private val questionGenerationService: QuestionGenerationService,
    private val followUpEvaluationService: FollowUpEvaluationService
) {
    fun startInterview(
        coverLetterText: String?,
        file: MultipartFile?,
        followUpEnabled: Boolean
    ): InterviewSession {
        val text = when {
            file != null && !file.isEmpty -> textExtractorService.extractText(
                file.inputStream, file.originalFilename ?: "file"
            )
            !coverLetterText.isNullOrBlank() -> coverLetterText
            else -> throw CoverLetterTooShortException("이력서 내용을 입력하거나 파일을 업로드해주세요.")
        }
        if (text.trim().length < 10) {
            throw CoverLetterTooShortException("이력서 내용이 너무 짧습니다. (최소 10자 이상)")
        }

        val jobField = jobDetectionService.detectJobField(text)
        val questions = questionGenerationService.generateQuestions(text, jobField)

        val session = InterviewSession(
            jobField = jobField,
            coverLetterText = text,
            followUpEnabled = followUpEnabled,
            questions = questions.toMutableList()
        )
        sessionStore.save(session)
        return session
    }

    fun submitAnswer(sessionId: String, questionId: String, answer: String): SubmitAnswerResponse {
        val session = sessionStore.findById(sessionId)
            ?: throw SessionNotFoundException("세션을 찾을 수 없습니다: $sessionId")

        val question = findQuestion(session, questionId)
            ?: throw SessionNotFoundException("질문을 찾을 수 없습니다: $questionId")
        question.answer = answer

        // depth=1: follow-up questions never generate more follow-ups
        if (!session.followUpEnabled || question.isFollowUp) {
            return SubmitAnswerResponse(needsFollowUp = false, followUpQuestion = null)
        }

        val result = followUpEvaluationService.evaluateAnswer(
            question = question.content,
            answer = answer,
            resume = session.coverLetterText
        )

        if (result.needsFollowUp && result.followUpQuestion != null) {
            val followUp = InterviewQuestion(
                content = result.followUpQuestion,
                category = "꼬리질문",
                isFollowUp = true
            )
            question.followUpQuestion = followUp
            return SubmitAnswerResponse(
                needsFollowUp = true,
                followUpQuestion = QuestionDto(
                    questionId = followUp.questionId,
                    content = followUp.content,
                    orderIndex = 0
                )
            )
        }
        return SubmitAnswerResponse(needsFollowUp = false, followUpQuestion = null)
    }

    fun completeInterview(sessionId: String): CompleteInterviewResponse {
        val session = sessionStore.findById(sessionId)
            ?: throw SessionNotFoundException("세션을 찾을 수 없습니다: $sessionId")
        if (session.status == SessionStatus.COMPLETED) {
            throw SessionAlreadyCompletedException("이미 완료된 면접 세션입니다.")
        }
        session.status = SessionStatus.COMPLETED
        session.completedAt = Instant.now()
        return CompleteInterviewResponse(
            sessionId = session.sessionId,
            jobField = session.jobField,
            completedAt = session.completedAt.toString()
        )
    }

    private fun findQuestion(session: InterviewSession, questionId: String): InterviewQuestion? {
        for (q in session.questions) {
            if (q.questionId == questionId) return q
            if (q.followUpQuestion?.questionId == questionId) return q.followUpQuestion
        }
        return null
    }
}
