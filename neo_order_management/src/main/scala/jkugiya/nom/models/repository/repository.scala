package jkugiya.nom.models

import jkugiya.nom.utils.neo4j.Connection
import org.anormcypher.Neo4jREST

import scala.concurrent.ExecutionContext

package object repository {
  implicit def toNeo4jConnection(implicit connection: Connection[_]): Neo4jREST = connection.con
  implicit def toExecutionContext(implicit connection: Connection[_]): ExecutionContext = connection.ec
}
