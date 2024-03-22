package club.pisquad.uiharu.qqbot.websocket.dto

import club.pisquad.uiharu.qqbot.command.trimCommandContent
import io.ktor.util.logging.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent")

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
) {
    init {
        LOGGER.debug("Created attachment $filename")
    }
}

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
) {
    init {
        LOGGER.debug("Created MessageCreateEvent: {}", this)
    }
}

fun MessageCreateEvent.formatAtContent(): String {
    return content.replace(Regex("""<@!(\d{20})>""")) { result: MatchResult ->
        var username = ""
        mentions.forEach() {
            if (it.id == result.groupValues[1]) {
                username = "@${it.username}"
            }
        }
        username
    }
}

fun MessageCreateEvent.trimAtContent(): String {
    return content.replace(Regex("""<@!(\d{20})>"""), "")
}

fun MessageCreateEvent.isCommand(): Boolean {
    val regex = Regex("""^ ?/\S{1,12}""")
    return regex.find(trimAtContent()) != null
}

fun MessageCreateEvent.parseCommand(): List<String> {
    return trimCommandContent().split(" ")
}