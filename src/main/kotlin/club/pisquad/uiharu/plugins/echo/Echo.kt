package club.pisquad.uiharu.plugins.echo

import club.pisquad.uiharu.plugins.Plugin
import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.command.QQBotCommandManager
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import club.pisquad.uiharu.qqbot.websocket.dto.parseCommand
import club.pisquad.uiharu.qqbot.command.QQBotCommand as QQBotCommandInterface

object Echo : Plugin {
    override val name: String = "echo"
    override val description: String = "Let the bot say whatever you want!"
    override val document: String = "Supported platform: QQ guild"

    override suspend fun initiate() {
        QQBotCommandManager.register(QQBotCommand)
    }

    internal object QQBotCommand : QQBotCommandInterface {
        override val name: String = "echo"
        override val usage: String = "echo <statement>"
        override val document: String = ""

        override suspend fun handle(event: MessageCreateEvent) {
            QQBotApi.sendChannelMessage(
                channelId = event.channelId,
                message = SendChannelMessageRequest(content = event.parseCommand().drop(1).joinToString(" "))
            )
        }
    }
}