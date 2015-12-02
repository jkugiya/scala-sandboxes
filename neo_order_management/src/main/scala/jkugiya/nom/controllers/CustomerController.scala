package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import jkugiya.nom.models.dto.customer.{RegisterCustomerDTO, UpdateCustomerDTO}
import jkugiya.nom.models.repository.CustomerRepository

import scala.concurrent.ExecutionContextExecutor

trait CustomerController {
  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  implicit val version: Int
  implicit val CustomerRepository: CustomerRepository

  val logger:  LoggingAdapter

  def route: Route = {
    pathPrefix("customers") {
      pathEnd {
        get {
          parameter('word) { word =>
            val customers = CustomerRepository.search(word)
            // todo render response
            ???
          }
          // TODO wordが無かったらどうなる？
        }
        post {
          parameter('name, 'email, 'tel, 'address, 'comment).as(RegisterCustomerDTO) { dto =>
            ???
          }
          // register
          ???
        }
        put {
          parameter('id.as[Long], 'name, 'email, 'tel, 'address, 'comment).as(UpdateCustomerDTO) { dto =>
            ???
          }
          // TODO update
          ???
        }
        delete {
          // TODO delete
          ???
        }
      } ~
      path(IntNumber) { id =>
        get {
          parameter('id.as[Int]) { id =>
            val customer = CustomerRepository.searchBy(id)
            // TODO render response
            ???
          }
        }
        delete {
          // delete
          ???
        }
      }
    }
  }
}
