package club.pisquad.uiharu.qqbot.command

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import io.ktor.util.logging.*

val LOGGER = KtorSimpleLogger("club.pisquad.uiharu.qqbot.command")

interface QQBotCommand {
    val name: String
    val usage: String
    val docs: String

    suspend fun handle(event: MessageCreateEvent)
}

object CommandNotFound : QQBotCommand {
    override val name: String = ""
    override val usage: String = ""
    override val docs: String = "command not found"

    override suspend fun handle(event: MessageCreateEvent) {
        QQBotApi.sendChannelMessage(event.channelId, SendChannelMessageRequest(content = "Command Not Found"))
    }

}

object CommandManager {
    private val commands: MutableMap<String, QQBotCommand> = mutableMapOf()


    init {
        register(AskAICommand)
    }

    private fun register(command: QQBotCommand) {
        // NOTE: Will replace command registered before
        commands[command.name] = command
        LOGGER.debug("New QQBotCommand added ${command.name}")
    }

    suspend fun handle(event: MessageCreateEvent) {
        LOGGER.debug("Handling Command {}", event)
        val command = commands.getOrDefault("ask-ai", CommandNotFound)
        LOGGER.debug("Command handled by {}", command)
        return command.handle(event)
    }
}