package com.interview.app.domain.model

data class CoverLetter(
    val text: String?,
    val filePath: String?,
    val fileType: CoverLetterType
)

enum class CoverLetterType { PDF, DOCX, TEXT }
