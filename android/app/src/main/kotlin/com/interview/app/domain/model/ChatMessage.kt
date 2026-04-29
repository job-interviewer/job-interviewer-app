package com.interview.app.domain.model

enum class MessageType { QUESTION, ANSWER, FOLLOWUP_QUESTION, FOLLOWUP_ANSWER }

data class ChatMessage(
    val type: MessageType,
    val content: String,
    val questionId: String?
)
