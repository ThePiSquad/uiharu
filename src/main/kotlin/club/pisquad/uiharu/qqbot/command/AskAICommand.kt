package club.pisquad.uiharu.qqbot.command

import club.pisquad.uiharu.AppConfiguration
import club.pisquad.uiharu.qqbot.api.QQBotApi
import club.pisquad.uiharu.qqbot.api.dto.SendChannelMessageRequest
import club.pisquad.uiharu.qqbot.websocket.dto.MessageCreateEvent
import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.models.QwenParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.MessageManager
import com.alibaba.dashscope.common.Role
import com.alibaba.dashscope.utils.Constants
import io.ktor.client.statement.*

internal object AskAiUtils {
    private val gen: Generation = Generation()
    private val messageManager: MessageManager = MessageManager()

    init {
        Constants.apiKey = AppConfiguration.getString("dashScopeApiKey")
    }

    init {
        messageManager.add(
            Message.builder().role(Role.SYSTEM.value).content(
                """
                    你叫作 Uiharu
                    你是粉墨战队的 御用风纪委员
                    粉墨战队是一个学生社团
                    你负责协助粉墨战队的成员开展活动
                """.trimIndent()
            ).build()
        )

    }

    fun getResponse(message: String): String {
        val param = QwenParam.builder().model(Generation.Models.QWEN_PLUS).messages(messageManager.get())
            .resultFormat(QwenParam.ResultFormat.MESSAGE).enableSearch(true).build();

        param.prompt = message
        val result = gen.call(param)
        messageManager.add(result)

        return result.output.choices[0].message.content
    }
}

object AskAICommand : QQBotCommand {
    override val name = "ask-ai"
    override val usage = "ask-ai"
    override val docs = "随机抓取幸运AI"

    override suspend fun handle(event: MessageCreateEvent) {
        val response = QQBotApi.sendChannelMessage(
            event.channelId, SendChannelMessageRequest(content = AskAiUtils.getResponse("介绍一下你自己"))
        )
        println(response.bodyAsText())
    }
}
