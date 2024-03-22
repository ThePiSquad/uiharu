package club.pisquad.uiharu.plugins.stealpfp

import club.pisquad.uiharu.plugins.Plugin
import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.command.QQBotCommandManager
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent

object StealPfp : Plugin {
    override val name: String = "stealpfp"
    override val description: String = "steal other people's pfp"
    override val document: String = "be care with the cops"

    override suspend fun initiate() {
        QQBotCommandManager.register(QQBotCommand)
    }

    internal object QQBotCommand : club.pisquad.uiharu.qqbot.command.QQBotCommand {
        override val name: String = "stealpfp"
        override val usage: String = "/stealpfp < @ that person in this message>"
        override val document: String = StealPfp.document

        private suspend fun sendImage(event: MessageCreateEvent, url: String) {
            QQBotApi.sendChannelMessage(
                channelId = event.channelId, SendChannelMessageRequest(
                    image = url
                )
            )
        }

        override suspend fun handle(event: MessageCreateEvent) {
            event.mentions.forEach {
                if (!it.bot) sendImage(event, it.avatar)
            }

        }
    }
}