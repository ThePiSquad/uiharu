package club.pisquad.uiharu.qqbot.websocket.dto

import club.pisquad.uiharu.qqbot.schemas.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IdentifyRequest(
    val token: String,
    val intents: Int,
    val shard: List<Int> = listOf(0, 1),
    val properties: String = "{}"
)

@Serializable
data class IdentifyResponse(
    val version: Int,
    @SerialName("session_id") val sessionId: String,
    val user: User,
    val shard: List<Int>
)