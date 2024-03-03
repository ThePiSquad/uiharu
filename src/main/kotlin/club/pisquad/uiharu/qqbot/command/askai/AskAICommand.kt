package club.pisquad.uiharu.qqbot.command.askai

import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.command.QQBotCommand
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import io.ktor.client.statement.*


object AskAICommand : QQBotCommand {
    override val name = "ask-ai"
    override val usage = "ask-ai"
    override val docs = "随机抓取幸运AI"

    override suspend fun handle(event: MessageCreateEvent) {
        val response = QQBotApi.sendChannelMessage(
            event.channelId, SendChannelMessageRequest(content = AskAiService.getResponse(event.content))
        )
        println(response.bodyAsText())
    }
}
