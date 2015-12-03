package jkugiya.nom.utils.neo4j

import com.google.inject.Inject
import jkugiya.nom.utils.{Global, NomModule}
import org.anormcypher.{CypherStatement, Cypher, Neo4jREST}
import play.api.libs.ws.WSClient

import scala.concurrent.ExecutionContext

abstract case class Namespace(name: String)

class Nom extends Namespace("nom")

trait Connection[A <: Namespace] {
  implicit val con: Neo4jREST
  implicit val ec: ExecutionContext
  val underlyingClient: WSClient
}

object Connection {
  implicit val nomCon: Connection[Nom] = {
    val global = implicitly[Global]
    global.injecor.getInstance(classOf[Connection[Nom]])
  }
}

class ConnectionImpl[A <: Namespace] @Inject() (
                                                 val con: Neo4jREST,
                                                 val ec: ExecutionContext,
                                                 val underlyingClient: WSClient) extends Connection[A]
