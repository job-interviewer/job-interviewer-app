package com.interview.server.service

import com.interview.server.model.InterviewSession
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionStore {
    private val sessions = ConcurrentHashMap<String, InterviewSession>()

    fun save(session: InterviewSession) {
        sessions[session.sessionId] = session
    }

    fun findById(sessionId: String): InterviewSession? = sessions[sessionId]
}
