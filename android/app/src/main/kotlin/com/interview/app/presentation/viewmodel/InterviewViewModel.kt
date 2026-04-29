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

    /**
     * Initializes the interview UI state for a new session using the provided session identifier, question list, and job field.
     *
     * Sets the state with the given `sessionId`, `jobField`, and `questions`; selects the first question (or `null`) as `currentQuestion` and, if present, seeds `chatMessages` with a single `QUESTION` message for that first question.
     *
     * @param sessionId Identifier for the interview session.
     * @param questions Ordered list of questions for the session.
     * @param jobField The job field associated with the session.
     */
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

    /**
     * Update the UI state's currentAnswer with the provided text.
     *
     * @param text The new answer text entered by the user.
     */
    fun onAnswerChanged(text: String) {
        _uiState.value = _uiState.value.copy(currentAnswer = text)
    }

    /**
     * Submits the current answer for the active question, updates the UI state, and handles the repository response.
     *
     * If there is no current question or the trimmed answer is empty, the call is a no-op. While submitting it sets the loading flag, appends an answer message to the chat, and on repository result either advances the interview flow (including follow-up questions) or clears the loading flag and records an error message.
     */
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

    /**
     * Updates the UI state in response to an answer result by advancing to a follow-up or next question,
     * updating chat messages and loading/completion flags, and triggering final completion when there are no more questions.
     *
     * @param answerResult The result from submitting an answer which indicates whether a follow-up question is required and provides that follow-up when present.
     * @param messages The current list of chat messages to use as the base when appending the next question or follow-up message.
     */
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

    /**
     * Finalizes the current interview session and persists its chat history.
     *
     * Calls the repository to mark the active session (from the current UI state) as completed
     * and saves the session ID, job field, and chat messages to local storage.
     */
    private fun completeInterview() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.completeInterview(state.sessionId)
            repository.saveSessionLocally(state.sessionId, state.jobField, state.chatMessages)
        }
    }
}
