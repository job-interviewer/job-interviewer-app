package com.interview.app.presentation.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.interview.app.domain.model.CoverLetter
import com.interview.app.domain.model.CoverLetterType
import com.interview.app.domain.model.Result
import com.interview.app.domain.repository.InterviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class InterviewSetupUiState(
    val followUpEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class InterviewSetupViewModel @Inject constructor(
    private val repository: InterviewRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(InterviewSetupUiState())
    val uiState: StateFlow<InterviewSetupUiState> = _uiState.asStateFlow()

    /**
     * Set whether follow-up questions should be included in the interview.
     *
     * @param enabled `true` to include follow-up questions, `false` to exclude them.
     */
    fun onFollowUpToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(followUpEnabled = enabled)
    }

    /**
     * Initiates an interview session using either raw text or a file and handles UI state updates.
     *
     * Starts the interview by building a CoverLetter from the provided `text` or `uri`, invokes the repository to start the interview, and updates UI state to reflect loading and any error message. On success, invokes `onSuccess` with the created session ID.
     *
     * @param text The cover letter text to use when `inputMode` is `InputMode.TEXT`; ignored for `InputMode.FILE`.
     * @param uri The content Uri of the file to use when `inputMode` is `InputMode.FILE`; must be non-null in that case.
     * @param inputMode Selects whether to use `text` or `uri` to construct the cover letter.
     * @param onSuccess Callback invoked with the session ID when the interview is started successfully.
     */
    fun startInterview(
        text: String?,
        uri: Uri?,
        inputMode: InputMode,
        onSuccess: (sessionId: String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val coverLetter = when (inputMode) {
                InputMode.TEXT -> CoverLetter(text = text, filePath = null, fileType = CoverLetterType.TEXT)
                InputMode.FILE -> {
                    val filePath = uriToFilePath(uri!!)
                    val fileType = if (filePath.endsWith(".pdf", true)) CoverLetterType.PDF else CoverLetterType.DOCX
                    CoverLetter(text = null, filePath = filePath, fileType = fileType)
                }
            }

            when (val result = repository.startInterview(coverLetter, _uiState.value.followUpEnabled)) {
                is Result.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess(result.data.sessionId)
                }
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
     * Converts a content `Uri` into a temporary file stored in the application's cache directory and returns its filesystem path.
     *
     * @param uri The content `Uri` to read and copy to a temporary file.
     * @return The absolute path of the created temporary file, or an empty string if the URI could not be opened. 
     */
    private fun uriToFilePath(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return ""
        val ext = context.contentResolver.getType(uri)?.let {
            if (it.contains("pdf")) ".pdf" else ".docx"
        } ?: ".pdf"
        val tempFile = File.createTempFile("resume", ext, context.cacheDir)
        inputStream.use { input -> tempFile.outputStream().use { input.copyTo(it) } }
        return tempFile.absolutePath
    }
}
