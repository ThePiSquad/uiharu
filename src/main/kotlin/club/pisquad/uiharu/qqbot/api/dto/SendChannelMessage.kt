package club.pisquad.uiharu.qqbot.api.dto

import club.pisquad.uiharu.qqbot.QQBotConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageEmbedThumbnail(
    val url: String,
)

@Serializable
data class MessageEmbedField(
    val name: String,
)

@Serializable
data class MessageEmbed(
    val title: String,
    val prompt: String,
    val thumbnail: MessageEmbedThumbnail,
    val fields: MessageEmbedField
)

@Serializable
data class MessageReference(
    @SerialName("message_id") val messageId: String,
    @SerialName("ignore_get_message_error") val ignoreGetMessageError: Boolean
)

@Serializable
data class TemplateParam(
    val key: String, val values: List<String>
)

@Serializable
data class MarkdownTemplateInner(
    @SerialName("custom_template_id") val templateId: String, val params: List<TemplateParam>
)

@Serializable
data class SendChannelMessageRequest(
     val content: String? = null,
     val embed: MessageEmbed? = null,
     @SerialName("message_reference") val messageReference: MessageReference? = null,
     @SerialName("msg_id") val msgId: String? = null,
     @SerialName("event_id") val eventId: String? = null,
     val markdown: MarkdownTemplateInner? = null
)

object MarkdownTemplateFactory {
    fun githubWebhookNotice(
        type: String,
        sender: String,
        installation: String,
        title1: String,
        content1: String,
        title2: String,
        content2: String,
    ): SendChannelMessageRequest {
        return SendChannelMessageRequest(
            markdown = MarkdownTemplateInner(
                templateId = QQBotConfiguration.getConfig("markdownTemplateId").getString("githubWebhook"),
                params = listOf(
                    TemplateParam("type", listOf(type)),
                    TemplateParam("sender", listOf(sender)),
                    TemplateParam("installation", listOf(installation)),
                    TemplateParam("title1", listOf(title1)),
                    TemplateParam("content1", listOf(content1)),
                    TemplateParam("title2", listOf(title2)),
                    TemplateParam("content2", listOf(content2)),
                )
            )
        )
    }
}