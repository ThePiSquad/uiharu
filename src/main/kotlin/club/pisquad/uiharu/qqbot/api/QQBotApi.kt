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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.time.LocalDateTime


const val URL_HOST: String = "https://api.sgroup.qq.com"

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
        println("Refreshing access token.")
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
            accessToken = data.accessToken
            accessTokenExpireTime = LocalDateTime.now().plusSeconds(data.expiresIn.toLong())
        }
    }

    private suspend fun callApi(
        type: HttpMethod, path: String, body: JsonElement? = null
    ): HttpResponse {
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
        callApi(
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
                    				"values": ["${content2}"]
                    			}
                    		]
                    	}
                    }
                """.trimIndent()
            )
        )
    }
}
