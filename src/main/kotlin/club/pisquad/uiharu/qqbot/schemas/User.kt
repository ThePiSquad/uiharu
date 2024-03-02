package club.pisquad.uiharu.qqbot.schemas

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val bot: Boolean
)