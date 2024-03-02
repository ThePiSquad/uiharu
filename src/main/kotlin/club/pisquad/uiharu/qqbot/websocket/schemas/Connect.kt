package club.pisquad.uiharu.qqbot.websocket.schemas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectResponseData(
    @SerialName("heartbeat_interval") val heartBeatInterval: Long
)