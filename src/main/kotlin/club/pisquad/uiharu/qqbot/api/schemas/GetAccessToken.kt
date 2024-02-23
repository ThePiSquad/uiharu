package club.pisquad.uiharu.qqbot.api.schemas

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAccessTokenRequest(
    val appId: String,
    val clientSecret: String
)

@Serializable
data class GetAccessTokenResponse(
    @SerialName("access_token") val accessToken: String,

    @SerialName("expires_in") val expiresIn: Int
)