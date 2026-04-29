package com.interview.app.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interview.app.domain.model.ChatMessage
import com.interview.app.domain.model.MessageType
import com.interview.app.domain.model.Question
import com.interview.app.presentation.viewmodel.InterviewViewModel

/**
 * Renders an interview chat UI for a given session and manages interaction flow.
 *
 * Observes the ViewModel state to display chat messages, a loading indicator, and an input row for answers.
 * Initializes the interview when `sessionId` changes, scrolls the message list to the newest item when messages update,
 * and invokes `onComplete(sessionId)` when the interview session is marked completed.
 *
 * @param sessionId Identifier of the interview session to render.
 * @param questions Initial list of questions to present during the interview.
 * @param jobField Job field label displayed in the top app bar.
 * @param followUpEnabled Enables follow-up question behavior when true.
 * @param onComplete Callback invoked with `sessionId` when the interview completes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InterviewScreen(
    sessionId: String,
    questions: List<Question>,
    jobField: String,
    followUpEnabled: Boolean,
    onComplete: (sessionId: String) -> Unit,
    viewModel: InterviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId, questions, jobField)
    }

    LaunchedEffect(uiState.chatMessages.size) {
        if (uiState.chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.chatMessages.size - 1)
        }
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) onComplete(sessionId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("면접 중 — ${uiState.jobField}") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(uiState.chatMessages) { message ->
                    ChatBubble(message)
                }
                if (uiState.isLoading) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }

            if (!uiState.isCompleted && !uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = uiState.currentAnswer,
                        onValueChange = viewModel::onAnswerChanged,
                        placeholder = { Text("답변을 입력하세요...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 5
                    )
                    IconButton(
                        onClick = viewModel::submitAnswer,
                        enabled = uiState.currentAnswer.isNotBlank()
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "전송")
                    }
                }
            }
        }
    }
}

/**
 * Displays a single chat message as a styled speech bubble, aligned and colored according to the message type.
 *
 * When the message is a follow-up question, a "꼬리질문" label is shown above the message content.
 *
 * @param message The chat message to render; its `type` determines alignment and bubble styling, and its `content` is shown as the bubble body.
 */
@Composable
private fun ChatBubble(message: ChatMessage) {
    val isQuestion = message.type == MessageType.QUESTION || message.type == MessageType.FOLLOWUP_QUESTION
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isQuestion) Arrangement.Start else Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    color = if (isQuestion) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                if (message.type == MessageType.FOLLOWUP_QUESTION) {
                    Text(
                        "꼬리질문",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(message.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
