package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.command.CommandManager
import club.pisquad.uiharu.qqbot.websocket.dto.EventType
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

object EventHandler {

    private val converter = Json {
        ignoreUnknownKeys = true
    }

    suspend fun handle(type: String, data: JsonElement) {

        when (type) {
            EventType.MESSAGE_CREATE.value, EventType.AT_MESSAGE_CREATE.value -> messageCreate(
                converter.decodeFromJsonElement(data)
            )

            else -> {
                LOGGER.debug("Unhandled event {}", data)
            }
        }
    }

    private suspend fun messageCreate(event: MessageCreateEvent) {
        CommandManager.handle(event)
    }
}