package club.pisquad.uiharu.qqbot.websocket

import club.pisquad.uiharu.qqbot.websocket.dto.OpCode
import club.pisquad.uiharu.qqbot.websocket.dto.PayloadBase
import kotlinx.serialization.json.JsonElement


internal object DispatchHandler {
    suspend fun handle(data: JsonElement) {

    }

}

object WebsocketHandler {
    suspend fun handle(content: PayloadBase<JsonElement>) {
        when (content.op) {
            OpCode.DISPATCH.value -> DispatchHandler.handle(content.d!!)
        }
    }
}