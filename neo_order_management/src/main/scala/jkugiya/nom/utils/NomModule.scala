package jkugiya.nom.utils

import com.google.inject.{Injector, AbstractModule, Singleton}
import jkugiya.nom.utils.neo4j.{ConnectionImpl, Nom, Connection}
import org.anormcypher.Neo4jREST
import play.api.libs.ws._

trait NomModule extends AbstractModule {
}

object NomModule {
  implicit lazy val module: NomModule = new NomModule {
    override def configure(): Unit = {
      val global = implicitly[Global]
      val config = global.config
      implicit val ws = ning.NingWSClient()
      val connection = Neo4jREST(
        host = config.getString("nom.neo4j.host"),
        port = config.getInt("nom.neo4j.port"),
        path = if (config.hasPath("nom.neo4j.path")) config.getString("nom.neo4j.path") else "/db/data/",
        username = config.getString("nom.neo4j.username"),
        password = config.getString("nom.neo4j.password")
      )
      val ec = scala.concurrent.ExecutionContext.Implicits.global// TODO
      bind(classOf[Connection[Nom]]).toInstance(new ConnectionImpl[Nom](connection, ec, ws))
    }
  }

}
