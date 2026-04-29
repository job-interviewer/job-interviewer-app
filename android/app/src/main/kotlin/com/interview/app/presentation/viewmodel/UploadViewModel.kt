package com.interview.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class UploadUiState(
    val selectedUri: Uri? = null,
    val inputText: String = "",
    val inputMode: InputMode = InputMode.TEXT
)

enum class InputMode { TEXT, FILE }

@HiltViewModel
class UploadViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UploadUiState())
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, inputMode = InputMode.TEXT)
    }

    fun onFileSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedUri = uri, inputMode = InputMode.FILE)
    }

    fun onModeChanged(mode: InputMode) {
        _uiState.value = _uiState.value.copy(inputMode = mode)
    }

    fun canProceed(): Boolean {
        return when (_uiState.value.inputMode) {
            InputMode.TEXT -> _uiState.value.inputText.length >= 10
            InputMode.FILE -> _uiState.value.selectedUri != null
        }
    }
}
