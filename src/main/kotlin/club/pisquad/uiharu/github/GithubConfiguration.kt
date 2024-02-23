package club.pisquad.uiharu.github

import club.pisquad.uiharu.AppConfiguration
import com.typesafe.config.Config

object GithubConfiguration : Config by AppConfiguration.getConfig("github") {
}