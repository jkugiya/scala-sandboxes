package jkugiya.nom.utils

import com.google.inject.{AbstractModule, Singleton}

trait NomModule extends AbstractModule

object NomModule {
  implicit val module: NomModule = new NomModule {
    override def configure(): Unit = {
      bind(classOf[Connection[Nom]]).to(classOf[ConnectionImpl[Nom]])in(classOf[Singleton])
    }
  }
}
