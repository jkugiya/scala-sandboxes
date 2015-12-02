package jkugiya.nom.models.entity

import jkugiya.nom.utils.neo4j.{Connection, Nom}
import org.anormcypher._

case class Customer(id: Long, name: String, email: String, tel: String, address: String, comment: String)

trait CustomerOps {

  private[entity] implicit val connection = implicitly[Connection[Nom]]

  // TODO to ResultSetParser
  def apply(row: CypherRow): Customer = Customer(
    id = row[Long]("c.id"),
    name = row[String]("c.name"),
    email = row[String]("c.email"),
    tel = row[String]("c.tel"),
    address = row[String]("c.address"),
    comment = row[String]("c.comment")
  )


  // TODO validation
  /*
  {{
  val result: Either[Errors, Customer] = isIdValid & isNameValid & is EmailValid ...
  for (customer <- result) { CustomerRepository.register(customer) }
  }}
   */

}

object Customer extends CustomerOps

