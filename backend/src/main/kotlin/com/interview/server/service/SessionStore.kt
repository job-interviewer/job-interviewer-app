package com.interview.server.service

import com.interview.server.model.InterviewSession
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class SessionStore {
    private val sessions = ConcurrentHashMap<String, InterviewSession>()

    /**
     * Stores an InterviewSession in the in-memory session store.
     *
     * Replaces any existing session that has the same `sessionId`.
     *
     * @param session The InterviewSession to store; `session.sessionId` is used as the map key.
     */
    fun save(session: InterviewSession) {
        sessions[session.sessionId] = session
    }

    /**
 * Retrieve an interview session by its session ID.
 *
 * @param sessionId The unique identifier of the session to look up.
 * @return The corresponding `InterviewSession` if present, `null` otherwise.
 */
fun findById(sessionId: String): InterviewSession? = sessions[sessionId]
}
