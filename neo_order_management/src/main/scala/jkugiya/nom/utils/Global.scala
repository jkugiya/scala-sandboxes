package jkugiya.nom.utils

import com.google.inject.{Injector, Guice}
import com.typesafe.config.{Config, ConfigFactory}

class Global(val config: Config)(implicit val module: NomModule) {
  lazy val injecor: Injector = Guice.createInjector(module)
}

object Global {
  implicit lazy val global = new Global(
    ConfigFactory.load()
  )
}