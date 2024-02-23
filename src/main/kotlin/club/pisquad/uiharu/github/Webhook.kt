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


object GithubWebhookHandler {
    suspend fun handle(meta: GithubWebhookEventMeta, payload: JsonObject) {
        when (meta.event) {
            "push" -> handlePush(meta, payload)
//            "issues" -> handleIssues(meta, payload)
            else -> {
                println("Unknown github webhook type ${meta.event}")
            }
        }
    }

    private suspend fun handlePush(meta: GithubWebhookEventMeta, payload: JsonObject) {

        val commitMessages = mutableListOf<String>();
        payload["commits"]?.jsonArray?.forEach {
            commitMessages.add(it.jsonObject.getValue("message").toString().replace("\"", ""))
        }


        QQBotApi.sendGithubWebhookNotice(
            type = meta.event,
            sender = payload["sender"]?.jsonObject?.getValue("login")?.toString()?.replace("\"", "") ?: "",
            installation = payload["organization"]?.jsonObject?.getValue("login")?.toString()?.replace("\"", "") ?: "",
            title1 = "commits",
            content1 = commitMessages.joinToString("; ")  // We are not allowed to have newline in markdown params
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