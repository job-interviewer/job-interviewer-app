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

    fun onFollowUpToggled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(followUpEnabled = enabled)
    }

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
