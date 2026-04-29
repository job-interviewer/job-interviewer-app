package com.interview.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.app.domain.model.AnswerResult
import com.interview.app.domain.model.ChatMessage
import com.interview.app.domain.model.MessageType
import com.interview.app.domain.model.Question
import com.interview.app.domain.model.Result
import com.interview.app.domain.repository.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewUiState(
    val sessionId: String = "",
    val jobField: String = "",
    val questions: List<Question> = emptyList(),
    val chatMessages: List<ChatMessage> = emptyList(),
    val currentQuestion: Question? = null,
    val currentAnswer: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InterviewViewModel @Inject constructor(
    private val repository: InterviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState: StateFlow<InterviewUiState> = _uiState.asStateFlow()

    fun initialize(sessionId: String, questions: List<Question>, jobField: String) {
        val first = questions.firstOrNull()
        _uiState.value = InterviewUiState(
            sessionId = sessionId,
            jobField = jobField,
            questions = questions,
            currentQuestion = first,
            chatMessages = if (first != null) listOf(
                ChatMessage(MessageType.QUESTION, first.content, first.questionId)
            ) else emptyList()
        )
    }

    fun onAnswerChanged(text: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = text)
    }

    fun submitAnswer() {
        val state = _uiState.value
        val question = state.currentQuestion ?: return
        val answer = state.currentAnswer.trim()
        if (answer.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)

            val updatedMessages = state.chatMessages + ChatMessage(
                MessageType.ANSWER, answer, question.questionId
            )

            when (val result = repository.submitAnswer(state.sessionId, question.questionId, answer)) {
                is Result.Success -> handleAnswerResult(result.data, updatedMessages)
                is Result.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private fun handleAnswerResult(answerResult: AnswerResult, messages: List<ChatMessage>) {
        val state = _uiState.value
        if (answerResult.needsFollowUp && answerResult.followUpQuestion != null) {
            val fq = answerResult.followUpQuestion
            _uiState.value = state.copy(
                isLoading = false,
                currentAnswer = "",
                currentQuestion = fq,
                chatMessages = messages + ChatMessage(MessageType.FOLLOWUP_QUESTION, fq.content, fq.questionId)
            )
        } else {
            val nextIndex = state.questions.indexOfFirst { it.questionId == state.currentQuestion?.questionId } + 1
            if (nextIndex < state.questions.size) {
                val next = state.questions[nextIndex]
                _uiState.value = state.copy(
                    isLoading = false,
                    currentAnswer = "",
                    currentQuestion = next,
                    chatMessages = messages + ChatMessage(MessageType.QUESTION, next.content, next.questionId)
                )
            } else {
                _uiState.value = state.copy(
                    isLoading = false,
                    currentAnswer = "",
                    currentQuestion = null,
                    chatMessages = messages,
                    isCompleted = true
                )
                completeInterview()
            }
        }
    }

    private fun completeInterview() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.completeInterview(state.sessionId)
            repository.saveSessionLocally(state.sessionId, state.jobField, state.chatMessages)
        }
    }
}
