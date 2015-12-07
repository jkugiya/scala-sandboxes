package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.{ConcreteMapper, Mappers}
import jkugiya.nom.models.dto.customer.{RegisterCustomerDTO, SearchCondition, UpdateCustomerDTO}
import jkugiya.nom.models.service.CustomerService
import jkugiya.nom.views

import scala.concurrent.ExecutionContextExecutor

trait CustomerController {
  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val mappers: Mappers = ConcreteMapper

  val version: Int = 1

  val customerService: CustomerService

  val logger: LoggingAdapter

  def completeAsHtml(body: String) = {
    complete(HttpResponse(
      entity = HttpEntity(ContentType(MediaTypes.`text/html`), body)
    ))
  }

  // TODO 全体的にEitherを使ったエラー処理
  /**
    * GET    /customers?word=xxx                  検索  => 検索結果一覧(customerSearch)
    * GET    /customers                               => 登録画面
    * GET    /customers/:id                       取得  => 更新画面(cutomerUpdate)
    * POST   /customers                           登録  => 検索結果一覧(customerSearch)
    * PUT    /customers/:id                       更新  => 検索結果一覧(customerSearch)
    * DELETE /customers/:id                       削除  => 検索結果一覧
    * @return [[Route]]
    */
  def route: Route = {
    pathPrefix("customers") {
      pathEnd {
        get {
          parameter('word).as(SearchCondition) { condition =>
            val customers = customerService.search(condition)
            completeAsHtml(views.html.customerSearch(condition.word, customers.right.get).body)
          }
        } ~
          post {
            // companionオブジェクトにメソッド定義するとこんな書き方をしないといけない。
            // 言語がエンハンスされることを期待、とのこと。
            formFields('name, 'email, 'tel, 'address, 'comment).as((RegisterCustomerDTO.apply _)) { condition =>
              customerService.register(condition)
              completeAsHtml(views.html.customerSearch("", Nil).body)
            }
          } ~
          put {
            formFields('id.as[Long], 'name, 'email, 'tel, 'address, 'comment).as(UpdateCustomerDTO) { condition =>
              customerService.update(condition)
              completeAsHtml(views.html.customerSearch("", Nil).body)
            }
          }
      } ~
        pathEndOrSingleSlash {
          get {
            // TODO パラメータエラー時の復元
            completeAsHtml(views.html.customerCreateForm(RegisterCustomerDTO.empty).body)
          }
        } ~
        pathSingleSlash {
          path(LongNumber) { customerId =>
            get {
              val result = customerService.findCustomer(customerId)
              val dto = mappers.map[Customer, UpdateCustomerDTO](result.right.get)// TODO エレガントに
              completeAsHtml(views.html.customerUpdateForm(dto).body)
            } ~
              delete {
                customerService.deleteCustomer(customerId)
                completeAsHtml(views.html.customerSearch("", Nil).body)
              }
          }
        }
    }
  }
}
