package club.pisquad.uiharu.qqbot.websocket.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReconnectRequest(
    val token: String,
    @SerialName("session_id") val sessionId: String,
    val seq: Int?,
)
