package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import jkugiya.nom.models.dto.customer.{RegisterCustomerDTO, SearchCondition, UpdateCustomerDTO}
import jkugiya.nom.models.service.CustomerService
import jkugiya.nom.views

import scala.concurrent.ExecutionContextExecutor

trait CustomerController {
  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val version: Int = 1

  val customerService: CustomerService

  val logger: LoggingAdapter

  def route: Route = {
    pathPrefix("customers") {
      pathEnd {
        get {
          parameter('word).as(SearchCondition) { condition =>
            val customers = customerService.search(condition)
            complete(views.html.customerSearch(condition.word, customers.right.get).body) // TODO
          }
        } ~
          post {
            formFields('name, 'email, 'tel, 'address, 'comment).as(RegisterCustomerDTO) { condition =>
              customerService.register(condition)
              complete(views.html.customerSearch("", Nil).body)
            }
          } ~
          put {
            formFields('id.as[Long], 'name, 'email, 'tel, 'address, 'comment).as(UpdateCustomerDTO) { condition =>
              customerService.update(condition)
              complete(views.html.customerSearch("", Nil).body)
            }
          }
      } ~
        pathSingleSlash {
          get {
            complete(views.html.customerSearch("", Nil).body)
          }
          path(LongNumber) { customerId =>
            get {
              val customer = customerService.findCustomer(customerId)
              complete(views.html.customerSearch("", Nil).body)
            } ~
              delete {
                customerService.deleteCustomer(customerId)
                complete(views.html.customerSearch("", Nil).body)
              }
          }
        }
    }
  }
}
