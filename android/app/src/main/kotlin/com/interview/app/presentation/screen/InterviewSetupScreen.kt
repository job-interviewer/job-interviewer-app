package com.interview.app.presentation.screen

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interview.app.presentation.viewmodel.InputMode
import com.interview.app.presentation.viewmodel.InterviewSetupViewModel

/**
 * Renders the interview setup screen that lets the user toggle follow-up question generation and start an interview.
 *
 * The UI displays an app bar titled "면접 설정", a description, a switch to enable or disable follow-up questions,
 * an optional error message, and a primary "면접 시작" button. Tapping the button starts an interview using the
 * provided cover letter (text or URI) and input mode; on successful start the provided callback is invoked with the
 * created session ID and the current follow-up setting.
 *
 * @param coverLetterText Optional cover letter text to include when starting the interview.
 * @param coverLetterUri Optional cover letter URI to include when starting the interview.
 * @param inputMode Configuration that determines how interview input is provided.
 * @param onInterviewStarted Callback invoked after a successful interview start with the new `sessionId` and
 *                          a `followUpEnabled` flag indicating whether follow-up questions were enabled.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewSetupScreen(
    coverLetterText: String?,
    coverLetterUri: Uri?,
    inputMode: InputMode,
    onInterviewStarted: (sessionId: String, followUpEnabled: Boolean) -> Unit,
    viewModel: InterviewSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("면접 설정") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("면접 설정을 확인하세요.", style = MaterialTheme.typography.titleMedium)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("꼬리질문 사용", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        "불충분한 답변에 추가 질문을 생성합니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = uiState.followUpEnabled,
                    onCheckedChange = viewModel::onFollowUpToggled
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.startInterview(
                        text = coverLetterText,
                        uri = coverLetterUri,
                        inputMode = inputMode,
                        onSuccess = { sessionId -> onInterviewStarted(sessionId, uiState.followUpEnabled) }
                    )
                },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                }
                Text("면접 시작")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
