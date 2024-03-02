package club.pisquad.uiharu.qqbot.websocket.schemas

import kotlinx.serialization.Serializable

@Serializable
enum class OpCode(val value: Int) {
    DISPATCH(0),
    HEARTBEAT(1),
    IDENTIFY(2),
    RESUME(6),
    RECONNECT(7),
    INVALID_SESSION(9),
    HELLO(10),
    HEARTBEAT_ACK(11),
    HTTP_CALLBACK_ACK(12);

    override fun toString(): String {
        return ordinal.toString()
    }

    companion object {
        fun valueOf(value: Int) = entries.find { it.value == value }
    }
}

@Serializable
data class PayloadBase<T>(
    //TODO: use enum class in here
    val op: Int,
    val d: T,
    val s: Int? = null,
    val t: String = "",
)