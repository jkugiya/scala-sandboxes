package jkugiya.nom.utils.neo4j

import com.google.inject.Inject
import org.anormcypher.Neo4jREST

abstract case class Namespace(name: String)

class Nom extends Namespace("nom")

trait Connection[A <: Namespace] {
  val con: Neo4jREST
}
class ConnectionImpl[A <: Namespace] @Inject() (val con: Neo4jREST) extends Connection[A]

object Connection {

}