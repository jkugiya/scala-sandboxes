package jkugiya.nom.utils

import com.typesafe.config.{Config, ConfigFactory}

class Global(val config: Config)

object Global {
  implicit lazy val global = new Global(
    ConfigFactory.load()
  )
}