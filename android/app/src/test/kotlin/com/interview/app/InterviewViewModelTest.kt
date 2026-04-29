package com.interview.app

import com.interview.app.domain.model.AnswerResult
import com.interview.app.domain.model.CoverLetter
import com.interview.app.domain.model.CoverLetterType
import com.interview.app.domain.model.InterviewSession
import com.interview.app.domain.model.Question
import com.interview.app.domain.model.Result
import com.interview.app.domain.repository.InterviewRepository
import com.interview.app.presentation.viewmodel.InterviewViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InterviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val repository = mockk<InterviewRepository>()
    private lateinit var viewModel: InterviewViewModel

    private val sampleQuestions = listOf(
        Question("q1", "Spring Boot 경험을 말씀해주세요.", 1),
        Question("q2", "MSA 환경 경험이 있으신가요?", 2)
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = InterviewViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialize sets first question as current`() {
        viewModel.initialize("session-1", sampleQuestions, "백엔드 개발")
        val state = viewModel.uiState.value
        assertEquals("session-1", state.sessionId)
        assertEquals("백엔드 개발", state.jobField)
        assertEquals("q1", state.currentQuestion?.questionId)
        assertEquals(1, state.chatMessages.size)
    }

    @Test
    fun `submitAnswer with followUp response shows followUp question`() = runTest {
        coEvery { repository.submitAnswer("session-1", "q1", any()) } returns
                Result.Success(AnswerResult(true, Question("fq1", "더 구체적으로 말씀해주세요.", 0)))

        viewModel.initialize("session-1", sampleQuestions, "백엔드 개발")
        viewModel.onAnswerChanged("네, 경험 있습니다.")
        viewModel.submitAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.chatMessages.size >= 2)
        assertEquals("fq1", state.currentQuestion?.questionId)
    }

    @Test
    fun `submitAnswer without followUp advances to next question`() = runTest {
        coEvery { repository.submitAnswer("session-1", "q1", any()) } returns
                Result.Success(AnswerResult(false, null))

        viewModel.initialize("session-1", sampleQuestions, "백엔드 개발")
        viewModel.onAnswerChanged("충분한 답변입니다.")
        viewModel.submitAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("q2", state.currentQuestion?.questionId)
    }

    @Test
    fun `submitting all answers marks interview as completed`() = runTest {
        coEvery { repository.submitAnswer("session-1", "q1", any()) } returns
                Result.Success(AnswerResult(false, null))
        coEvery { repository.submitAnswer("session-1", "q2", any()) } returns
                Result.Success(AnswerResult(false, null))
        coEvery { repository.completeInterview("session-1") } returns Result.Success(Unit)
        coEvery { repository.saveSessionLocally(any(), any(), any()) } returns Unit

        viewModel.initialize("session-1", sampleQuestions, "백엔드 개발")

        viewModel.onAnswerChanged("답변1")
        viewModel.submitAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onAnswerChanged("답변2")
        viewModel.submitAnswer()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isCompleted)
    }
}
