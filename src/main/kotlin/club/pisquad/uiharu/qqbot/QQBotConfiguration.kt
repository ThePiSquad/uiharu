package club.pisquad.uiharu.qqbot

import club.pisquad.uiharu.AppConfiguration
import com.typesafe.config.Config

object QQBotConfiguration : Config by AppConfiguration.getConfig("qqBot") {
    val appId: String = getString("appId")
    val clientSecret: String = getString("clientSecret")
}