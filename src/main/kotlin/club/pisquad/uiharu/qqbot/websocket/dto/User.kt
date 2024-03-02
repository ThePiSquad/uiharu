package club.pisquad.uiharu.qqbot.websocket.dto

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val avatar: String,
    val bot: Boolean,
    val id: String = "<NONE>",
    val username: String,
)