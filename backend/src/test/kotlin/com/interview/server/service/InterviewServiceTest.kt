package com.interview.server.service

import com.interview.server.dto.QuestionDto
import com.interview.server.dto.SubmitAnswerResponse
import com.interview.server.model.InterviewQuestion
import com.interview.server.model.InterviewSession
import com.interview.server.model.SessionStatus
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InterviewServiceTest {

    private val sessionStore = mockk<SessionStore>()
    private val textExtractorService = mockk<TextExtractorService>()
    private val jobDetectionService = mockk<JobDetectionService>()
    private val questionGenerationService = mockk<QuestionGenerationService>()
    private val followUpEvaluationService = mockk<FollowUpEvaluationService>()

    private val service = InterviewService(
        sessionStore,
        textExtractorService,
        jobDetectionService,
        questionGenerationService,
        followUpEvaluationService
    )

    private val sampleQuestions = listOf(
        InterviewQuestion(questionId = "q1", content = "질문1", category = "직무역량"),
        InterviewQuestion(questionId = "q2", content = "질문2", category = "프로젝트경험"),
        InterviewQuestion(questionId = "q3", content = "질문3", category = "문제해결"),
        InterviewQuestion(questionId = "q4", content = "질문4", category = "커뮤니케이션"),
        InterviewQuestion(questionId = "q5", content = "질문5", category = "성장목표")
    )

    @BeforeEach
    fun setUp() {
        every { sessionStore.save(any()) } just Runs
    }

    @Test
    fun `startInterview with text creates session with 5 questions`() {
        val resumeText = "안녕하세요. 3년차 백엔드 개발자입니다. Spring Boot와 Kotlin을 주로 사용합니다."
        every { jobDetectionService.detectJobField(resumeText) } returns "백엔드 개발"
        every { questionGenerationService.generateQuestions(resumeText, "백엔드 개발") } returns sampleQuestions

        val session = service.startInterview(resumeText, null, true)

        assertEquals("백엔드 개발", session.jobField)
        assertEquals(5, session.questions.size)
        assertEquals(true, session.followUpEnabled)
        verify { sessionStore.save(any()) }
    }

    @Test
    fun `startInterview with short text throws CoverLetterTooShortException`() {
        assertThrows<CoverLetterTooShortException> {
            service.startInterview("짧음", null, true)
        }
    }

    @Test
    fun `startInterview with null text and null file throws CoverLetterTooShortException`() {
        assertThrows<CoverLetterTooShortException> {
            service.startInterview(null, null, true)
        }
    }

    @Test
    fun `submitAnswer with followUpEnabled=false returns needsFollowUp=false`() {
        val session = createSessionWithQuestion("q1")
        every { sessionStore.findById(session.sessionId) } returns session

        val result = service.submitAnswer(session.sessionId, "q1", "답변입니다.")

        assertFalse(result.needsFollowUp)
        assertNull(result.followUpQuestion)
        verify(exactly = 0) { followUpEvaluationService.evaluateAnswer(any(), any(), any()) }
    }

    @Test
    fun `submitAnswer with insufficient answer triggers follow-up`() {
        val session = createSessionWithQuestion("q1", followUpEnabled = true)
        every { sessionStore.findById(session.sessionId) } returns session
        every { followUpEvaluationService.evaluateAnswer(any(), any(), any()) } returns
            FollowUpResult(true, "더 구체적으로 말씀해주세요.")

        val result = service.submitAnswer(session.sessionId, "q1", "충분히 긴 답변입니다. 이 답변은 10자 이상입니다.")

        assertTrue(result.needsFollowUp)
        assertNotNull(result.followUpQuestion)
    }

    @Test
    fun `submitAnswer on follow-up question never generates more follow-ups`() {
        val followUpQ = InterviewQuestion(questionId = "fq1", content = "꼬리질문", category = "꼬리질문", isFollowUp = true)
        val mainQ = InterviewQuestion(questionId = "q1", content = "메인질문", category = "직무역량", followUpQuestion = followUpQ)
        val session = InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "이력서 내용입니다.",
            followUpEnabled = true,
            questions = mutableListOf(mainQ)
        )
        every { sessionStore.findById(session.sessionId) } returns session

        val result = service.submitAnswer(session.sessionId, "fq1", "짧은 답변")

        assertFalse(result.needsFollowUp)
        verify(exactly = 0) { followUpEvaluationService.evaluateAnswer(any(), any(), any()) }
    }

    @Test
    fun `completeInterview marks session as completed`() {
        val session = InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "이력서",
            followUpEnabled = true
        )
        every { sessionStore.findById(session.sessionId) } returns session

        val result = service.completeInterview(session.sessionId)

        assertEquals(session.sessionId, result.sessionId)
        assertEquals(SessionStatus.COMPLETED, session.status)
        assertNotNull(session.completedAt)
    }

    @Test
    fun `completeInterview on already completed session throws SessionAlreadyCompletedException`() {
        val session = InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "이력서",
            followUpEnabled = true,
            status = SessionStatus.COMPLETED
        )
        every { sessionStore.findById(session.sessionId) } returns session

        assertThrows<SessionAlreadyCompletedException> {
            service.completeInterview(session.sessionId)
        }
    }

    @Test
    fun `submitAnswer on unknown session throws SessionNotFoundException`() {
        every { sessionStore.findById("unknown") } returns null

        assertThrows<SessionNotFoundException> {
            service.submitAnswer("unknown", "q1", "답변")
        }
    }

    private fun createSessionWithQuestion(
        questionId: String,
        followUpEnabled: Boolean = false
    ): InterviewSession {
        val question = InterviewQuestion(questionId = questionId, content = "테스트 질문", category = "직무역량")
        return InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "이력서 내용입니다.",
            followUpEnabled = followUpEnabled,
            questions = mutableListOf(question)
        )
    }
}
