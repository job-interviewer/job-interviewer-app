package com.interview.server.service

class GeminiApiException(message: String) : RuntimeException(message)
class CoverLetterTooShortException(message: String) : RuntimeException(message)
class SessionNotFoundException(message: String) : RuntimeException(message)
class SessionAlreadyCompletedException(message: String) : RuntimeException(message)

