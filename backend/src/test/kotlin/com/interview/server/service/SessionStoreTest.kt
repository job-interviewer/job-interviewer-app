package com.interview.server.service

import com.interview.server.model.InterviewSession
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SessionStoreTest {

    private val store = SessionStore()

    @Test
    fun `save and findById returns session`() {
        val session = InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "테스트 이력서",
            followUpEnabled = true
        )
        store.save(session)
        val found = store.findById(session.sessionId)
        assertNotNull(found)
        assertEquals(session.sessionId, found!!.sessionId)
        assertEquals("백엔드 개발", found.jobField)
    }

    @Test
    fun `findById returns null for unknown id`() {
        val result = store.findById("nonexistent-id")
        assertNull(result)
    }

    @Test
    fun `save overwrites existing session`() {
        val session = InterviewSession(
            jobField = "백엔드 개발",
            coverLetterText = "이력서",
            followUpEnabled = true
        )
        store.save(session)
        session.status = com.interview.server.model.SessionStatus.COMPLETED
        store.save(session)
        val found = store.findById(session.sessionId)
        assertEquals(com.interview.server.model.SessionStatus.COMPLETED, found!!.status)
    }
}
