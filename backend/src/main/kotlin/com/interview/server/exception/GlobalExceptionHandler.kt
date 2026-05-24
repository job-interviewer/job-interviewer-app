package com.interview.server.exception

import com.interview.server.dto.ErrorResponse
import com.interview.server.service.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
         * Map a SessionNotFoundException to an HTTP 404 response containing an ErrorResponse.
         *
         * @param e The thrown SessionNotFoundException.
         * @return A ResponseEntity with status 404 and an ErrorResponse whose `code` is `"SESSION_NOT_FOUND"`
         *         and whose `message` is the exception message or the default "세션을 찾을 수 없습니다.".
         */
        @ExceptionHandler(SessionNotFoundException::class)
    fun handleNotFound(e: SessionNotFoundException) =
        ResponseEntity.status(404).body(ErrorResponse("SESSION_NOT_FOUND", e.message ?: "세션을 찾을 수 없습니다."))

    /**
         * Handles a CoverLetterTooShortException by returning an HTTP 400 response with an error payload.
         *
         * @return A ResponseEntity containing an ErrorResponse with code `COVER_LETTER_TOO_SHORT` and the exception message or the default "이력서 내용이 너무 짧습니다." if the message is null.
         */
        @ExceptionHandler(CoverLetterTooShortException::class)
    fun handleTooShort(e: CoverLetterTooShortException) =
        ResponseEntity.status(400).body(ErrorResponse("COVER_LETTER_TOO_SHORT", e.message ?: "이력서 내용이 너무 짧습니다."))

    /**
         * Handles UnsupportedFileException and responds with a 400 Bad Request indicating an unsupported file type.
         *
         * @param e The caught UnsupportedFileException.
         * @return A ResponseEntity containing an ErrorResponse with code "UNSUPPORTED_FILE_TYPE" and the exception message or "지원하지 않는 파일 형식입니다." if the message is null.
         */
        @ExceptionHandler(UnsupportedFileException::class)
    fun handleUnsupported(e: UnsupportedFileException) =
        ResponseEntity.status(400).body(ErrorResponse("UNSUPPORTED_FILE_TYPE", e.message ?: "지원하지 않는 파일 형식입니다."))

    /**
         * Handle cases where a document contains no extractable text and respond with HTTP 400.
         *
         * @return ResponseEntity containing an ErrorResponse with code `"EMPTY_DOCUMENT"` and the exception's message or the default message "파일에서 텍스트를 추출할 수 없습니다."
         */
        @ExceptionHandler(EmptyDocumentException::class)
    fun handleEmpty(e: EmptyDocumentException) =
        ResponseEntity.status(400).body(ErrorResponse("EMPTY_DOCUMENT", e.message ?: "파일에서 텍스트를 추출할 수 없습니다."))

    /**
         * Responds with HTTP 400 when a session has already been completed.
         *
         * @param e The thrown SessionAlreadyCompletedException; its message is used as the response message if present.
         * @return A ResponseEntity containing an ErrorResponse with code "SESSION_ALREADY_COMPLETED" and the exception message or the default "이미 완료된 세션입니다.".
         */
        @ExceptionHandler(SessionAlreadyCompletedException::class)
    fun handleAlreadyCompleted(e: SessionAlreadyCompletedException) =
        ResponseEntity.status(400).body(ErrorResponse("SESSION_ALREADY_COMPLETED", e.message ?: "이미 완료된 세션입니다."))

    /**
         * Maps a GeminiApiException to an HTTP 503 response.
         *
         * @param e The caught GeminiApiException.
         * @return A ResponseEntity containing an ErrorResponse with code `LLM_SERVICE_ERROR` and a message taken from the exception or `"AI 서비스 오류가 발생했습니다."` if the exception message is null.
         */
        @ExceptionHandler(GeminiApiException::class)
    fun handleGemini(e: GeminiApiException) =
        ResponseEntity.status(503).body(ErrorResponse("LLM_SERVICE_ERROR", e.message ?: "AI 서비스 오류가 발생했습니다."))

    /**
         * Converts any uncaught exception into an HTTP 500 response with a standardized error payload.
         *
         * @param e The caught exception.
         * @return A ResponseEntity containing an ErrorResponse with code "INTERNAL_ERROR" and message "서버 오류가 발생했습니다.", returned with HTTP status 500.
         */
        @ExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception) =
        ResponseEntity.status(500).body(ErrorResponse("INTERNAL_ERROR", "서버 오류가 발생했습니다."))
}
