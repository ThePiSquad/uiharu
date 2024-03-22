package club.pisquad.uiharu.qqbot.command

import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent

interface QQBotCommand {
    val name: String
    val usage: String
    val document: String

    suspend fun handle(event: MessageCreateEvent) {}
}

fun MessageCreateEvent.trimCommandContent(): String {
    return content.replace(Regex("""<@!(\d{20})>"""), "").trim()
}

fun MessageCreateEvent.parseCommand(): List<String> {
    return trimCommandContent().split(" ")
}

object QQBotCommandManager {
    private val commands: MutableMap<String, QQBotCommand> = mutableMapOf()

    fun register(command: QQBotCommand) {
        commands[command.name] = command
    }

    suspend fun handle(event: MessageCreateEvent) {
        val commandArgs = event.parseCommand()
        val commandName = commandArgs[0].substring(1)
        val command = commands.getOrDefault(commandName, CommandNotFound)
        command.handle(event)
    }
}