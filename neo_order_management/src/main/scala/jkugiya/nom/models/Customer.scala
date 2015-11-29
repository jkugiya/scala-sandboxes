package jkugiya.nom.models

import jkugiya.nom.utils.neo4j.{Nom, Connection}
import org.anormcypher.{CypherRow, Cypher}

case class Customer(id: Long, name: String, email: String, tel: String, address: String, comment: String)

object Customer {
  def apply(row: CypherRow): Customer = Customer(
    id = row[Long]("c.id"),
    name = row[String]("c.name"),
    email = row[String]("c.email"),
    tel = row[String]("c.tel"),
    address = row[String]("c.address"),
    comment = row[String]("c.comment")
  )
}



trait CustomerDAO {

  def search(word: String)(implicit connection: Connection[Nom]): Seq[Customer] = {
    val query = Cypher(
      """
        | MATCH (c: Customer)
        | WHERE c.name =~ {wordExpression}
        | OR    c.email =~ {wordExpression}
        | OR    c.tel =~ {wordExpression}
        | OR    c.address =~ {wordExpression}
        | OR    c.comment =~ {wordExpression}
        | RETURN c.id, c.name, c.email, c.tel, c.address, c.comment
      """.stripMargin).on("wordExpression" -> s".*${word}.*")
    query.apply().map(Customer.apply).toSeq
  }

  def searchBy(id: Long)(implicit connection: Connection[Nom]): Option[Customer] = {
    Cypher(
      """
        | MATCH (c: Customer)
        | WHERE c.id = {id}
        | RETURN c.id, c.name, c.email, c.tel, c.address, c.comment
      """.stripMargin)
      .on("id" -> id)
      .apply()
      .headOption.map(Customer.apply)
  }

  def create(customer: Customer)(implicit connection: Connection[Nom]): Unit = {
    Cypher(
      """
        |MERGE (id: UniqueId { name: 'Customer' })
        |ON CREATE SET id.count = 1
        |ON MATCH SET id.count = id.count + 1
        |WITH id.count AS uid
        |
        |CREATE (c: Customer
        |{id: uid,
        | name: {name},
        | email: {email},
        | tel: {tel},
        | address: {address},
        | comment: {comment} }) return c.id""".stripMargin)
      .on("name" -> customer.name,
        "email" -> customer.email,
        "tel" -> customer.tel,
        "address" -> customer.address,
        "comment" -> customer.comment)
      .apply()
  }

  def update(customer: Customer)(implicit con: Connection[Nom]): Unit = {
    Cypher(
      """
        |MATCH (c: Customer { id: {id} })
        |SET
        |  c.name = {name},
        |  c.email = {email},
        |  c.tel = {tel},
        |  c.address = {address},
        |  c.comment = {comment}
        |RETURN c.id, c.name, c.email, c.tel, c.address, c.comment
      """.stripMargin)
      .on("id" -> customer.id,
        "name" -> customer.name,
        "email" -> customer.email,
        "tel" -> customer.tel,
        "address" -> customer.address,
        "comment" -> customer.comment)
      .apply()
  }

  def remove(customer: Customer)(implicit con: Connection[Nom]): Unit = {
    Cypher(
      """
        |MATCH (p:Customer { id: {id} })
        |DELETE p
      """.stripMargin)
    .on("id" -> customer.id)
    .apply()
  }
}


