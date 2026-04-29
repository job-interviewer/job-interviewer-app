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

    /**
     * Updates the upload UI state with the given text and sets the input mode to TEXT.
     *
     * @param text The new text entered by the user.
     */
    fun onTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text, inputMode = InputMode.TEXT)
    }

    /**
     * Updates the UI state with the provided file `Uri` and sets the input mode to `FILE`.
     *
     * @param uri The `Uri` of the selected file.
     */
    fun onFileSelected(uri: Uri) {
        _uiState.value = _uiState.value.copy(selectedUri = uri, inputMode = InputMode.FILE)
    }

    /**
     * Set the current input mode in the upload UI state.
     *
     * @param mode The new input mode (e.g., `InputMode.TEXT` or `InputMode.FILE`) to apply.
     */
    fun onModeChanged(mode: InputMode) {
        _uiState.value = _uiState.value.copy(inputMode = mode)
    }

    /**
     * Determines whether the current upload input meets the mode-specific requirements to proceed.
     *
     * For `TEXT` mode, the text must have at least 10 characters. For `FILE` mode, a file URI must be selected.
     *
     * @return `true` if the current input satisfies the requirement for the active `InputMode`, `false` otherwise.
     */
    fun canProceed(): Boolean {
        return when (_uiState.value.inputMode) {
            InputMode.TEXT -> _uiState.value.inputText.length >= 10
            InputMode.FILE -> _uiState.value.selectedUri != null
        }
    }
}
