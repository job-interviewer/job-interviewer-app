package com.interview.app.domain.model

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val message: String, val code: String? = null) : Result<Nothing>
}
