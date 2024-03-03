package club.pisquad.uiharu.qqbot.command.askai

import club.pisquad.uiharu.AppConfiguration
import com.alibaba.dashscope.aigc.generation.Generation
import com.alibaba.dashscope.aigc.generation.models.QwenParam
import com.alibaba.dashscope.common.Message
import com.alibaba.dashscope.common.MessageManager
import com.alibaba.dashscope.common.Role
import com.alibaba.dashscope.utils.Constants

internal object AskAiService {
    private val gen: Generation = Generation()
    private val messageManager: MessageManager = MessageManager(10)

    init {
        Constants.apiKey = AppConfiguration.getString("dashScopeApiKey")
    }

    init {
        messageManager.add(
            Message.builder().role(Role.SYSTEM.value).content(
                """
                    你叫作 Uiharu
                    你的名字来源于 魔法禁书目录 中 初春饰利 的罗马音 Uiharu Kazari
                    你是粉墨战队的 御用风纪委员
                    粉墨战队是一个学生社团
                    你负责协助粉墨战队的成员开展活动
                """.trimIndent()
            ).build()
        )

    }

    fun getResponse(message: String): String {
        messageManager.add(Message.builder().role(Role.USER.value).content(message).build())

        val param = QwenParam.builder().model(Generation.Models.QWEN_PLUS).messages(messageManager.get())
            .resultFormat(QwenParam.ResultFormat.MESSAGE).enableSearch(true).build()

        val result = gen.call(param)

        messageManager.add(result)

        return result.output.choices[0].message.content
    }
}