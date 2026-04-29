package com.interview.app.presentation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.interview.app.presentation.viewmodel.InputMode
import com.interview.app.presentation.viewmodel.UploadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    onProceed: (text: String?, uri: Uri?, mode: InputMode) -> Unit,
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("이력서 업로드") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = uiState.inputMode == InputMode.TEXT,
                    onClick = { viewModel.onModeChanged(InputMode.TEXT) },
                    label = { Text("텍스트 입력") }
                )
                FilterChip(
                    selected = uiState.inputMode == InputMode.FILE,
                    onClick = { viewModel.onModeChanged(InputMode.FILE) },
                    label = { Text("파일 업로드") }
                )
            }

            if (uiState.inputMode == InputMode.TEXT) {
                OutlinedTextField(
                    value = uiState.inputText,
                    onValueChange = viewModel::onTextChanged,
                    label = { Text("이력서 내용을 입력하세요") },
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = { filePicker.launch("application/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.selectedUri != null) "파일 선택됨 ✓" else "PDF/DOCX 파일 선택")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onProceed(
                        if (uiState.inputMode == InputMode.TEXT) uiState.inputText else null,
                        if (uiState.inputMode == InputMode.FILE) uiState.selectedUri else null,
                        uiState.inputMode
                    )
                },
                enabled = viewModel.canProceed(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("다음")
            }
        }
    }
}
