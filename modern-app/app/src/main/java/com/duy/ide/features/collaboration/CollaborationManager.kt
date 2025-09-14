package com.duy.ide.features.collaboration

import javax.inject.Inject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class CollaborationManager @Inject constructor() {
    private val sessions = ConcurrentHashMap<String, CollaborationSession>()
    private val userSessions = ConcurrentHashMap<String, String>()
    private val nextSessionId = AtomicInteger(0)

    fun createSession(projectPath: String, host: User): String {
        val sessionId = "session_${nextSessionId.incrementAndGet()}"
        val session = CollaborationSession(
            id = sessionId,
            projectPath = projectPath,
            host = host,
            participants = mutableSetOf(host)
        )
        sessions[sessionId] = session
        userSessions[host.id] = sessionId
        return sessionId
    }

    fun joinSession(sessionId: String, user: User): Boolean {
        val session = sessions[sessionId] ?: return false
        session.participants.add(user)
        userSessions[user.id] = sessionId
        notifyParticipantJoined(session, user)
        return true
    }

    fun leaveSession(userId: String) {
        val sessionId = userSessions[userId] ?: return
        val session = sessions[sessionId] ?: return
        val user = session.participants.find { it.id == userId } ?: return
        
        session.participants.remove(user)
        userSessions.remove(userId)
        
        if (session.participants.isEmpty()) {
            sessions.remove(sessionId)
        } else if (user == session.host) {
            // Transferir host para outro participante
            session.host = session.participants.first()
        }
        
        notifyParticipantLeft(session, user)
    }

    fun broadcastEdit(sessionId: String, edit: CollaborativeEdit) {
        val session = sessions[sessionId] ?: return
        session.participants.forEach { participant ->
            if (participant.id != edit.userId) {
                sendEditToParticipant(participant, edit)
            }
        }
    }

    fun broadcastCursor(sessionId: String, cursor: CursorPosition) {
        val session = sessions[sessionId] ?: return
        session.participants.forEach { participant ->
            if (participant.id != cursor.userId) {
                sendCursorToParticipant(participant, cursor)
            }
        }
    }

    fun broadcastChat(sessionId: String, message: ChatMessage) {
        val session = sessions[sessionId] ?: return
        session.participants.forEach { participant ->
            sendChatToParticipant(participant, message)
        }
    }

    private fun notifyParticipantJoined(session: CollaborationSession, user: User) {
        session.participants.forEach { participant ->
            if (participant.id != user.id) {
                sendJoinNotification(participant, user)
            }
        }
    }

    private fun notifyParticipantLeft(session: CollaborationSession, user: User) {
        session.participants.forEach { participant ->
            sendLeaveNotification(participant, user)
        }
    }
}

data class CollaborationSession(
    val id: String,
    val projectPath: String,
    var host: User,
    val participants: MutableSet<User>
)

data class User(
    val id: String,
    val name: String,
    val color: Int
)

data class CollaborativeEdit(
    val userId: String,
    val filePath: String,
    val position: Int,
    val oldText: String,
    val newText: String,
    val timestamp: Long
)

data class CursorPosition(
    val userId: String,
    val filePath: String,
    val line: Int,
    val column: Int
)

data class ChatMessage(
    val userId: String,
    val userName: String,
    val message: String,
    val timestamp: Long
)