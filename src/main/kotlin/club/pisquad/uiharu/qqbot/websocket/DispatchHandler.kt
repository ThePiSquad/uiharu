package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.command.CommandManager
import club.pisquad.uiharu.qqbot.websocket.dto.EventType
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import club.pisquad.uiharu.qqbot.websocket.dto.PayloadBase
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

internal object DispatchHandler {

    private val eventConverter = Json { ignoreUnknownKeys = true }
    suspend fun handle(payload: PayloadBase<JsonElement>) {
        when (payload.t) {
            EventType.MESSAGE_CREATE.value, EventType.AT_MESSAGE_CREATE.value -> handleMessageCreate(
                eventConverter.decodeFromJsonElement<MessageCreateEvent>(
                    payload.d!!
                )
            )

            else -> {
                LOGGER.debug("Unhandled dispatch {}", payload)
            }
        }
    }

    private suspend fun handleMessageCreate(event: MessageCreateEvent) {
        LOGGER.debug("handling MessageCreateEvent")
        CommandManager.handle(event)
    }

}