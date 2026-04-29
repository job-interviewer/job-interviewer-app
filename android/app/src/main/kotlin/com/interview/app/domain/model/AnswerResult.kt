package com.interview.app.domain.model

data class AnswerResult(
    val needsFollowUp: Boolean,
    val followUpQuestion: Question?
)
