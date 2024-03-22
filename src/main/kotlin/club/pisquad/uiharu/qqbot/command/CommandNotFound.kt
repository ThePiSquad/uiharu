package club.pisquad.uiharu.qqbot.command

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent

object CommandNotFound : QQBotCommand {
    override val name: String = "command-not-found"
    override val usage: String = ""
    override val document: String = ""
    override suspend fun handle(event: MessageCreateEvent) {
        QQBotApi.sendChannelMessage(
            channelId = event.channelId,
            SendChannelMessageRequest(content = "command not found")
        )
    }
}