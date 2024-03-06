package club.pisquad.uiharu.qqbot.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponse(
    val message: String,
    val code: Int,
    @SerialName("err_code") val errorCode: Int,
    @SerialName("trace_id") val traceId: String,
)