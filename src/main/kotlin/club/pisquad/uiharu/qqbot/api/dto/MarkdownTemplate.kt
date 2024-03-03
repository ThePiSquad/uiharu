package club.pisquad.uiharu.qqbot.api.dto

import club.pisquad.uiharu.qqbot.QQBotConfiguration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarkdownTemplate(
    val markdown: MarkdownTemplateInner
)

@Serializable
data class TemplateParam(
    val key: String, val values: List<String>
)

@Serializable
data class MarkdownTemplateInner(
    @SerialName("custom_template_id") val templateId: String, val params: List<TemplateParam>
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
    ): MarkdownTemplate {
        return MarkdownTemplate(
            MarkdownTemplateInner(
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
