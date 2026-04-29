package com.interview.server.service

import com.interview.server.model.InterviewQuestion
import com.interview.server.model.InterviewSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SessionStoreTest {
    private val store = SessionStore()

    @Test
    fun `save and findById returns saved session`() {
        val session = makeSession()
        store.save(session)
        assertEquals(session.sessionId, store.findById(session.sessionId)?.sessionId)
    }

    @Test
    fun `findById returns null for unknown id`() {
        assertNull(store.findById("nonexistent-id"))
    }

    private fun makeSession() = InterviewSession(
        jobField = "백엔드 개발",
        coverLetterText = "테스트 이력서입니다.",
        followUpEnabled = true,
        questions = mutableListOf(InterviewQuestion(content = "질문1", category = "직무역량"))
    )
}
