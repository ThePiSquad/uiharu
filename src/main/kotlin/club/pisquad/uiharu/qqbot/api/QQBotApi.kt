package club.pisquad.uiharu.qqbot.api

import club.pisquad.uiharu.qqbot.QQBotConfiguration
import club.pisquad.uiharu.qqbot.api.dto.*
import club.pisquad.uiharu.trimQuotes
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.logging.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import java.time.LocalDateTime


const val URL_HOST: String = "https://api.sgroup.qq.com"

internal val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.api.QQBotApi")

object QQBotApi {

    lateinit var accessToken: String
    private lateinit var accessTokenExpireTime: LocalDateTime
    private val jsonConverter = Json {
        ignoreUnknownKeys = true
    }

    init {
        runBlocking { getAppAccessToken() }
    }

    private fun getClient(): HttpClient {
        if (accessTokenExpireTime < LocalDateTime.now()) {
            runBlocking {
                getAppAccessToken()
            }
        }

        return HttpClient(CIO) {
            defaultRequest {
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Authorization", "QQBot $accessToken")
                header("X-Union-Appid", QQBotConfiguration.appId)
            }
            install(ContentNegotiation) {
                json()
            }
        }
    }

    private fun handleError(api: String, body: String) {
        val error = jsonConverter.decodeFromString<ApiErrorResponse>(body)
        LOGGER.error("API $api error with code ${error.code} message ${error.message} ")
    }

    private suspend fun getAppAccessToken() {
        LOGGER.debug("Refreshing access token")
        // We are not using getClient() here because access_token has not been retrieved yet
        val response = HttpClient(CIO) {
            install(ContentNegotiation) {
                json()
            }
        }.post("https://bots.qq.com/app/getAppAccessToken") {
            header("Content-Type", "application/json")
            header("Accept", "application/json")
            setBody(GetAccessTokenRequest(QQBotConfiguration.appId, QQBotConfiguration.clientSecret))
        }

        if (response.status == HttpStatusCode.OK) {
            val data = response.body<GetAccessTokenResponse>()
            val expiresIn = data.expiresIn.toLong()
            accessToken = data.accessToken
            accessTokenExpireTime = LocalDateTime.now().plusSeconds(expiresIn)
            LOGGER.debug("[QQBOT] New access token retrieved $accessToken expire in $expiresIn")
        } else {
            handleError("getAppAccessToken", response.body())
        }
    }

    private suspend fun callApi(
        type: HttpMethod, path: String, body: String? = null
    ): HttpResponse {
        LOGGER.debug("Calling api [{}] {}", type.toString(), path)
        val response = getClient().request("${URL_HOST}${path}") {
            method = type
            setBody(body)
        }

        if (response.status != HttpStatusCode.OK) {
            handleError(path, response.bodyAsText())
        }

        return response
    }

    suspend fun sendGithubWebhookNotice(
        type: String,
        sender: String,
        installation: String,
        title1: String = "No",
        content1: String = "content",
        title2: String = "No",
        content2: String = "content",
    ) {
        LOGGER.debug("Creating GithubWebhookNotice with args $type $sender $installation $title1 $content1 $title2 $content2")
        val response = sendChannelMessage(
            QQBotConfiguration.getConfig("channel").getString("githubNotice"),
            MarkdownTemplateFactory.githubWebhookNotice(
                type,
                sender,
                installation,
                title1,
                content1,
                title2,
                content2,
            )
        )
    }

    suspend fun getWebsocketGateway(): String {
        val response = callApi(HttpMethod.Get, "/gateway")
        val gatewayUrl = Json.parseToJsonElement(response.bodyAsText()).jsonObject["url"].toString().trimQuotes()
        LOGGER.debug("Response gateway $gatewayUrl")
        return gatewayUrl
    }

    suspend fun sendChannelMessage(channelId: String, message: SendChannelMessageRequest): HttpResponse {
        return callApi(HttpMethod.Post, "/channels/$channelId/messages", body = jsonConverter.encodeToString(message))
    }
}