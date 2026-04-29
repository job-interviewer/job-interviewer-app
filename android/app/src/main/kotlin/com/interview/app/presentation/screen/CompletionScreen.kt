package com.interview.app.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interview.app.presentation.viewmodel.CompletionViewModel

/**
 * Displays the completion screen for an interview session.
 *
 * Shows a scaffolded screen titled "면접 완료" that presents a completion message, the job field from
 * the view model state, an optional summary list of chat messages (labelled by message type), and a
 * full-width "처음으로" button.
 *
 * @param sessionId Identifier of the interview session.
 * @param onRestart Callback invoked when the "처음으로" button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionScreen(
    sessionId: String,
    onRestart: () -> Unit,
    viewModel: CompletionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("면접 완료") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("면접이 완료되었습니다!", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("직무: ${uiState.jobField}", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (uiState.chatMessages.isNotEmpty()) {
                item { Text("면접 내용 요약", style = MaterialTheme.typography.titleMedium) }
                items(uiState.chatMessages) { message ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = when (message.type.name) {
                                    "QUESTION" -> "Q"
                                    "ANSWER" -> "A"
                                    "FOLLOWUP_QUESTION" -> "꼬리Q"
                                    else -> "꼬리A"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(message.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("처음으로")
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
