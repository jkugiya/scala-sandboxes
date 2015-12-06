package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import jkugiya.nom.models.dto.customer.{SearchCondition, RegisterCustomerDTO, UpdateCustomerDTO}
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.CustomerService

import scala.concurrent.ExecutionContextExecutor

trait CustomerController {
  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  implicit val version: Int

  val customerService: CustomerService

  val logger:  LoggingAdapter

  def route: Route = {
    pathPrefix("customers") {
      pathEnd {
        get {
          parameter('word).as(SearchCondition) { condition =>
            val customers = customerService.search(condition)
            ???
          }
          // TODO wordが無かったらどうなる？
        }
        post {
          parameter('name, 'email, 'tel, 'address, 'comment).as(RegisterCustomerDTO) { condition =>
            customerService.register(condition)
            ???
          }
          // register
          ???
        }
        put {
          parameter('id.as[Long], 'name, 'email, 'tel, 'address, 'comment).as(UpdateCustomerDTO) { condition =>
            customerService.update(condition)
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
          parameter('id.as[Long]) { customerId =>
            val customer = customerService.findCustomer(customerId)
            // TODO render response
            ???
          }
        }
        delete {
          parameter('id.as[Long]) { customerId =>
            customerService.deleteCustomer(customerId)
            // TOOD render response
            ???
          }
        }
      }
    }
  }
}
