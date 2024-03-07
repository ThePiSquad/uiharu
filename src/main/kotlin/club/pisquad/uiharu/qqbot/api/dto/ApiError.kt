package club.pisquad.uiharu.qqbot.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ApiErrorResponse(
    val message: String,
    val code: Int,
    @Transient @SerialName("err_code") val errorCode: Int? = null,
    @Transient @SerialName("trace_id") val traceId: String? = null,
)