package com.interview.server.service

import com.interview.server.model.InterviewQuestion
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InterviewServiceTest {
    private val sessionStore = SessionStore()
    private val textExtractorService = mockk<TextExtractorService>()
    private val jobDetectionService = mockk<JobDetectionService>()
    private val questionGenerationService = mockk<QuestionGenerationService>()
    private val followUpEvaluationService = mockk<FollowUpEvaluationService>()

    private val service = InterviewService(
        sessionStore, textExtractorService, jobDetectionService,
        questionGenerationService, followUpEvaluationService
    )

    private val sampleResume = "3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용하며 MSA 환경에서 결제 시스템을 구축한 경험이 있습니다."

    @Test
    fun `startInterview with valid text creates session with 5 questions`() {
        every { jobDetectionService.detectJobField(any()) } returns "백엔드 개발"
        every { questionGenerationService.generateQuestions(any(), any()) } returns makeQuestions()

        val session = service.startInterview(sampleResume, null, true)

        assertEquals(5, session.questions.size)
        assertEquals("백엔드 개발", session.jobField)
        assertTrue(session.followUpEnabled)
    }

    @Test
    fun `startInterview with short text throws CoverLetterTooShortException`() {
        assertThrows<CoverLetterTooShortException> {
            service.startInterview("짧음", null, true)
        }
    }

    @Test
    fun `startInterview with null text throws CoverLetterTooShortException`() {
        assertThrows<CoverLetterTooShortException> {
            service.startInterview(null, null, true)
        }
    }

    @Test
    fun `submitAnswer with followUpEnabled false never returns needsFollowUp`() {
        every { jobDetectionService.detectJobField(any()) } returns "백엔드 개발"
        every { questionGenerationService.generateQuestions(any(), any()) } returns makeQuestions()

        val session = service.startInterview(sampleResume, null, false)
        val q = session.questions[0]

        val result = service.submitAnswer(session.sessionId, q.questionId, "답변 내용")

        assertFalse(result.needsFollowUp)
    }

    @Test
    fun `submitAnswer on followUp question enforces depth=1`() {
        every { jobDetectionService.detectJobField(any()) } returns "백엔드 개발"
        every { questionGenerationService.generateQuestions(any(), any()) } returns makeQuestions()
        every { followUpEvaluationService.evaluateAnswer(any(), any(), any()) } returns
                FollowUpResult(true, "더 구체적으로 설명해주세요.")

        val session = service.startInterview(sampleResume, null, true)
        val q = session.questions[0]

        val firstResult = service.submitAnswer(session.sessionId, q.questionId, "네")
        assertTrue(firstResult.needsFollowUp)
        assertNotNull(firstResult.followUpQuestion)

        val fqId = firstResult.followUpQuestion!!.questionId
        val secondResult = service.submitAnswer(session.sessionId, fqId, "짧은 답변")
        assertFalse(secondResult.needsFollowUp)
    }

    @Test
    fun `completeInterview twice throws SessionAlreadyCompletedException`() {
        every { jobDetectionService.detectJobField(any()) } returns "백엔드 개발"
        every { questionGenerationService.generateQuestions(any(), any()) } returns makeQuestions()

        val session = service.startInterview(sampleResume, null, true)
        service.completeInterview(session.sessionId)

        assertThrows<SessionAlreadyCompletedException> {
            service.completeInterview(session.sessionId)
        }
    }

    private fun makeQuestions() = listOf(
        InterviewQuestion(content = "Spring Boot 경험에 대해 설명하세요.", category = "직무역량"),
        InterviewQuestion(content = "MSA 프로젝트에서 어떤 역할을 했나요?", category = "프로젝트경험"),
        InterviewQuestion(content = "복잡한 기술적 문제를 어떻게 해결했나요?", category = "문제해결"),
        InterviewQuestion(content = "팀 내 소통 방식은 어떠했나요?", category = "커뮤니케이션"),
        InterviewQuestion(content = "앞으로 어떤 개발자가 되고 싶나요?", category = "성장목표")
    )
}
