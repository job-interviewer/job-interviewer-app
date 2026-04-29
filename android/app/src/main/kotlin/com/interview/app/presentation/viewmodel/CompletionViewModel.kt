package com.interview.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.interview.app.domain.model.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CompletionUiState(
    val sessionId: String = "",
    val jobField: String = "",
    val chatMessages: List<ChatMessage> = emptyList()
)

@HiltViewModel
class CompletionViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(CompletionUiState())
    val uiState: StateFlow<CompletionUiState> = _uiState.asStateFlow()

    fun initialize(sessionId: String, jobField: String, messages: List<ChatMessage>) {
        _uiState.value = CompletionUiState(sessionId = sessionId, jobField = jobField, chatMessages = messages)
    }
}
