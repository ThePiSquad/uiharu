package club.pisquad.uiharu.github

import club.pisquad.uiharu.qqbot.api.QQBotApi
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

data class GithubWebhookEventMeta(
    val hookId: String,
    val event: String,
    val delivery: String,
    val signature: String,
    val userAgent: String,
    val installationTargetType: String,
    val installationTargetId: String,
)

fun String.trimQuotes(): String {
    return this.replace("\"", "")
}

internal object PayloadUtils {
    fun getSender(payload: JsonObject): String {
        // Somehow there is " in the result of toString
        return payload["sender"]?.jsonObject?.getValue("login")?.toString()?.trimQuotes() ?: "<no provided>"
    }

    fun getInstallation(payload: JsonObject): String {
        // Somehow there is " in the result of toString
        return payload["organization"]?.jsonObject?.getValue("login")?.toString()?.trimQuotes() ?: "<no provided>"
    }

    fun getRepoName(payload: JsonObject): String {
        return payload["repository"]?.jsonObject?.getValue("full_name")?.toString()?.trimQuotes() ?: "<no provided>"
    }

    fun shortenContent(text: String): String {
        if (text.length < 30) {
            return text
        }

        return text.substring(0, 30) + "...more..."
    }

}

object GithubWebhookHandler {
    suspend fun handle(meta: GithubWebhookEventMeta, payload: JsonObject) {
        when (meta.event) {
            "push" -> handlePush(meta, payload)
            "issues" -> handleIssues(meta, payload)
            else -> {
                println("Unknown github webhook type ${meta.event}")
            }
        }
    }

    private suspend fun handlePush(meta: GithubWebhookEventMeta, payload: JsonObject) {
        val sender = PayloadUtils.getSender(payload)
        val installation = PayloadUtils.getInstallation(payload)
        val repo = PayloadUtils.getRepoName(payload)

        val commitMessages = mutableListOf<String>()
        payload["commits"]?.jsonArray?.forEach {
            commitMessages.add(it.jsonObject.getValue("message").toString().replace("\"", ""))
        }
        val commits = commitMessages.joinToString("  ||  ")  // We are not allowed to have newline in markdown params

        QQBotApi.sendGithubWebhookNotice(
            type = meta.event,
            sender = sender,
            installation = installation,
            title1 = "Repo",
            content1 = repo,
            title2 = "Commits",
            content2 = commits
        )
    }

    private suspend fun handleIssues(meta: GithubWebhookEventMeta, payload: JsonObject) {
        val sender = PayloadUtils.getSender(payload)
        val repo = PayloadUtils.getRepoName(payload)

        val action = payload.getValue("action").toString().trimQuotes()
        val issueTitle = payload["issue"]?.jsonObject?.getValue("title")?.toString()?.trimQuotes() ?: "<no provided>"
        val issueContent =
            PayloadUtils.shortenContent(payload["issue"]?.jsonObject?.getValue("body")?.toString() ?: "<no provided>")
                .trimQuotes()
        QQBotApi.sendGithubWebhookNotice(
            type = "${meta.event}[$action]",
            sender = sender,
            installation = repo,
            title1 = "Title",
            content1 = issueTitle,
            title2 = "Content",
            content2 = issueContent

        )
    }
}


fun Application.githubWebhook() {
    routing {
        post("github") {
            val headers = call.request.headers
            val meta = GithubWebhookEventMeta(
                hookId = headers["X-GitHub-Hook-ID"] ?: "",
                event = headers["X-GitHub-Event"] ?: "",
                delivery = headers["X-GitHub-Delivery"] ?: "",
                signature = headers["X-Hub-Signature"] ?: "",
                userAgent = headers["User-Agent"] ?: "",
                installationTargetType = headers["X-GitHub-Hook-Installation-Target-Type"] ?: "",
                installationTargetId = headers["X-GitHub-Hook-Installation-Target-ID"] ?: "",
            )
            val body = call.receiveText()
            val json = Json.parseToJsonElement(body).jsonObject
            GithubWebhookHandler.handle(meta, json)
        }
    }
}