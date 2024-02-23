package club.pisquad.uiharu

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object AppConfiguration : Config by ConfigFactory.load()
