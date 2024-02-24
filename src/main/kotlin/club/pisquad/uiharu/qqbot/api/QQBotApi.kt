package club.pisquad.uiharu.qqbot.api

import club.pisquad.uiharu.qqbot.QQBotConfiguration
import club.pisquad.uiharu.qqbot.api.schemas.GetAccessTokenRequest
import club.pisquad.uiharu.qqbot.api.schemas.GetAccessTokenResponse
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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.LocalDateTime


const val URL_HOST: String = "https://api.sgroup.qq.com"

internal val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.api.QQBotApi")

object QQBotApi {

    private lateinit var accessToken: String
    private lateinit var accessTokenExpireTime: LocalDateTime

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

    private suspend fun getAppAccessToken() {
        LOGGER.debug("[QQBOT] Refreshing access token")
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
            accessTokenExpireTime = LocalDateTime.now().plusSeconds(data.expiresIn.toLong())
            LOGGER.debug("[QQBOT] New access token retrieved $accessToken expire in $expiresIn")
        } else {
            LOGGER.error("[QQBOT] Refresh access token failed with message ${response.bodyAsText()}")
        }
    }

    private suspend fun callApi(
        type: HttpMethod, path: String, body: JsonElement? = null
    ): HttpResponse {
        LOGGER.debug("Calling api [{}] {}", type.toString(), path)
        return getClient().request("${URL_HOST}${path}") {
            method = type
            setBody(body.toString())
        }
    }

    suspend fun sendGithubWebhookNotice(
        type: String,
        sender: String,
        installation: String,
        title1: String? = "No",
        content1: String? = "content",
        title2: String? = "No",
        content2: String? = "content",
    ) {
        LOGGER.debug("Creating GithubWebhookNotice with args $type $sender $installation $title1 $content1 $title2 $content2")
        val response = callApi(
            HttpMethod.Post,
            "/channels/${QQBotConfiguration.getConfig("channel").getString("githubNotice")}/messages",
            body = Json.parseToJsonElement(
                """
                    {
                    	"markdown": {
                    		"custom_template_id": "102089083_1708578737",
                    		"params": [{
                    				"key": "type",
                    				"values": ["$type"]
                    			},
                    			{
                    				"key": "sender",
                    				"values": ["$sender"]
                    			},
                    			{
                    				"key": "installation",
                    				"values": ["$installation"]
                    			},
                    			{
                    				"key": "title1",
                    				"values": ["$title1"]
                    			},
                    			{
                    				"key": "content1",
                    				"values": ["$content1"]
                    			},
                    			{
                    				"key": "title2",
                    				"values": ["$title2"]
                    			},
                    			{
                    				"key": "content2",
                    				"values": ["$content2"]
                    			}
                    		]
                    	}
                    }
                """.trimIndent()
            )
        )
        when (response.status) {
            HttpStatusCode.OK -> LOGGER.debug("Successfully created GithubWebhookNotice")
            else -> LOGGER.error("Create GithubWebhook Failed ${response.bodyAsText()}")
        }
    }
}
