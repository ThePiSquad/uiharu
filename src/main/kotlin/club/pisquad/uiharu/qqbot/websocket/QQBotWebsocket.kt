package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.websocket.dto.*
import club.pisquad.uiharu.qqbot.websocket.exception.ConnectionError
import club.pisquad.uiharu.qqbot.websocket.exception.IdentifyError
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.util.logging.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import java.util.*
import kotlin.concurrent.schedule

internal val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.websocket.QQBotWebsocket")

object QQBotWebsocket {
    private lateinit var gatewayUrl: String;
    private var heartBeatTimer: Timer? = null
    private var sessionId: String? = null
    private var client: HttpClient = HttpClient(CIO) {
        install(WebSockets)
    }

    //TODO: use middleware for automatically tracing the value
    private var latestSerialNumber: Int? = null

    suspend fun run() {
        gatewayUrl = QQBotApi.getWebsocketGateway();
        LOGGER.debug("Connecting to websocket using gatewayUrl $gatewayUrl")
        while (true) {
            client = HttpClient(CIO) {
                install(WebSockets) {
                    contentConverter = KotlinxWebsocketSerializationConverter(Json { ignoreUnknownKeys = true })
                }
            }
            try {
                client.webSocket({
                    url.takeFrom(gatewayUrl)
                }) {
                    // Process first connect response
                    try {
                        val connectResponse = receiveDeserialized<PayloadBase<ConnectResponse>>()
                        if (connectResponse.op != OpCode.HELLO.value) {
                            LOGGER.error("Connect to websocket failed with response $connectResponse")
                            throw ConnectionError()
                        }
                        setHeartbeatTimer(connectResponse.d!!.heartBeatInterval, this)
                    } catch (e: Exception) {
                        throw ConnectionError()
                    }

                    // Identify
                    try {
                        sendSerialized(
                            PayloadBase(
                                op = OpCode.IDENTIFY.value,
                                d = IdentifyRequest("QQBot " + QQBotApi.accessToken, intents = INTENT_VALUE)
                            )
                        )
                        val identifyResponse = receiveDeserialized<PayloadBase<IdentifyResponse>>()
                        if (identifyResponse.op != OpCode.DISPATCH.value) {
                            // QQ use DISPATCH event to indicate identify succeed
                            throw IdentifyError()
                        }
                        sessionId = identifyResponse.d!!.sessionId
                    } catch (e: Exception) {
                        throw IdentifyError()
                    }
                    // Listen event
                    while (true) {
                        val event = receiveDeserialized<PayloadBase<JsonElement>>()
                        when (event.op) {
                            OpCode.HEARTBEAT_ACK.value -> {
                                LOGGER.debug("received heartbeat ACK")
                            }

                            OpCode.DISPATCH.value -> {
                                LOGGER.debug("received new dispatch")
                                DispatchHandler.handle(event)
                            }

                            else -> {
                                LOGGER.debug("unhandled event")
                            }
                        }
                        latestSerialNumber = event.s ?: latestSerialNumber
                    }
                }
            } catch (e: ConnectionError) {
                LOGGER.error(e)
            } catch (e: IdentifyError) {
                LOGGER.error(e)
            } catch (e: Exception) {
                LOGGER.error("Unknown error", e)
            }

        }
    }

    private suspend fun setHeartbeatTimer(period: Long, session: DefaultClientWebSocketSession) {
        if (heartBeatTimer == null) {
            LOGGER.debug("Cancelling websocket heartbeat timer")
        }
        heartBeatTimer = Timer()
        // TODO: Thread safety
        heartBeatTimer!!.schedule(delay = period, period = period) {
            runBlocking {
                LOGGER.debug("Sending heartbeat")
                session.sendSerialized(PayloadBase(op = OpCode.HEARTBEAT.value, d = latestSerialNumber))
                //NOTE: We cannot receive and handle response here,
                // there will be conflicts with other places which are listening to the incoming messages
            }
        }
    }
}

fun Application.QQBotWebsocket() {
    launch { QQBotWebsocket.run() }
}