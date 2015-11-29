package jkugiya.nom

import jkugiya.nom.utils.neo4j.Connection
import org.anormcypher.Neo4jREST

import scala.concurrent.ExecutionContext

/**
  * Created by jkugi_000 on 2015/11/28.
  */
package object models {
  implicit def toNeo4jConnection(implicit connection: Connection[_]): Neo4jREST = connection.con
  implicit def toExecutionContext(implicit connection: Connection[_]): ExecutionContext = connection.ec
}
