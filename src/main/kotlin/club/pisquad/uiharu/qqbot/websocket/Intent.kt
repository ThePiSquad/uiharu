package club.pisquad.uiharu.qqbot.websocket


enum class Intent(val value: Int) {
    GUILDS(1 shl 0),
    GUILD_MEMBERS(1 shl 1),
    GUILD_MESSAGES(1 shl 9),
    GUILD_MESSAGE_REACTIONS(1 shl 10),
    DIRECT_MESSAGE(1 shl 12),
    INTERACTION(1 shl 26),
    MESSAGE_AUDIT(1 shl 27),
    FORUMS_EVENT(1 shl 28),
    AUDIO_ACTION(1 shl 29),
    PUBLIC_GUILD_MESSAGES(1 shl 30),
}

val INTENT_VALUE: Int = 0 or Intent.PUBLIC_GUILD_MESSAGES.value