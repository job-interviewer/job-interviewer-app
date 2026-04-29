package com.interview.server.exception

import com.interview.server.dto.ErrorResponse
import com.interview.server.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException::class)
    fun handleNotFound(e: SessionNotFoundException) =
        ResponseEntity.status(404).body(ErrorResponse("SESSION_NOT_FOUND", e.message ?: "세션을 찾을 수 없습니다."))

    @ExceptionHandler(CoverLetterTooShortException::class)
    fun handleTooShort(e: CoverLetterTooShortException) =
        ResponseEntity.status(400).body(ErrorResponse("COVER_LETTER_TOO_SHORT", e.message ?: "이력서 내용이 너무 짧습니다."))

    @ExceptionHandler(UnsupportedFileException::class)
    fun handleUnsupported(e: UnsupportedFileException) =
        ResponseEntity.status(400).body(ErrorResponse("UNSUPPORTED_FILE_TYPE", e.message ?: "지원하지 않는 파일 형식입니다."))

    @ExceptionHandler(EmptyDocumentException::class)
    fun handleEmpty(e: EmptyDocumentException) =
        ResponseEntity.status(400).body(ErrorResponse("EMPTY_DOCUMENT", e.message ?: "파일에서 텍스트를 추출할 수 없습니다."))

    @ExceptionHandler(SessionAlreadyCompletedException::class)
    fun handleAlreadyCompleted(e: SessionAlreadyCompletedException) =
        ResponseEntity.status(400).body(ErrorResponse("SESSION_ALREADY_COMPLETED", e.message ?: "이미 완료된 세션입니다."))

    @ExceptionHandler(GeminiApiException::class)
    fun handleGemini(e: GeminiApiException) =
        ResponseEntity.status(503).body(ErrorResponse("LLM_SERVICE_ERROR", e.message ?: "AI 서비스 오류가 발생했습니다."))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception) =
        ResponseEntity.status(500).body(ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."))
}
