package club.pisquad.uiharu.qqbot.websocket.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// NOTE:We only process at_message, so the attachments will always be image type
@Serializable
data class Attachment(
    val filename: String,
    val height: Int,
    val width: Int,
    val id: String,
    val size: String,
    val url: String,
    @SerialName("content_type") val contentType: String,
)

@Serializable
data class MessageCreateEvent(
    val id: String,
    val seq: Int,
    val author: User,
    val content: String,
    val timestamp: String,
    val mentions: List<User> = listOf(),
    val attachments: List<Attachment> = listOf(),
    @SerialName("guild_id") val guildId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("seq_in_channel") val seqInChannel: Int
)