package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.model.StatusCodes.Redirection
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{StandardRoute, Route}
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.Inject
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.{ConcreteMapper, Mappers}
import jkugiya.nom.models.dto.customer.{RegisterCustomerDTO, SearchCondition, UpdateCustomerDTO}
import jkugiya.nom.models.service.{Result, CustomerService}
import jkugiya.nom.utils.Global
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
  // TODO POSTやPUT表示しているところはRedirectにする
  // TODO ホットデプロイ的なことできないのか？
  /**
    * GET    /customers?word=xxx                  検索  => 検索結果一覧(customerSearch)
    * GET    /customers/create                         => 登録画面
    * GET    /customers                                => 検索結果一覧
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
            handleResult(customerService.search(condition)) { customers =>
              completeAsHtml(views.html.customerSearch(condition.word, customers).body)
            }
          }
        } ~
          post {
            // companionオブジェクトにメソッド定義するとこんな書き方をしないといけない。
            // 言語がエンハンスされることを期待、とのこと。
            formFields('name, 'email, 'tel, 'address, 'comment).as((RegisterCustomerDTO.apply _)) { condition =>
              handleResult(customerService.register(condition)) { _ =>
                redirect("/customers", StatusCodes.MovedPermanently)
              }
            }
          }
      } ~
        pathEndOrSingleSlash {
          get {
            completeAsHtml(views.html.customerSearch("", Nil).body)
          }
        } ~
        path("create") {
          get {
            completeAsHtml(views.html.customerCreateForm(RegisterCustomerDTO.empty).body)
          }
        } ~
        path(LongNumber) { customerId =>
          (put | parameter('method ! "put")) {
            formFields('id ? customerId, 'name, 'email, 'tel, 'address, 'comment).as(UpdateCustomerDTO) { condition =>
              handleResult(customerService.update(condition)) { _ =>
                redirect("/customers", StatusCodes.MovedPermanently)
              }
            }
          } ~
            (delete | parameter('method ! "delete")) {
              handleResult(customerService.deleteCustomer(customerId)) { _ =>
                redirect("/customers", StatusCodes.MovedPermanently)
              }
            } ~
            get {
              handleResult(customerService.findCustomer(customerId)) { customer =>
                val dto = mappers.map[Customer, UpdateCustomerDTO](customer) // TODO エレガントに
                completeAsHtml(views.html.customerUpdateForm(dto).body)
              }
            }
        }
    }
  }
  def handleResult[A](result: Result[A])(f: A => StandardRoute) = result match {
    case Right(a) => f(a)
    case Left(error) =>
      error.cause.fold(logger.error(error.message))(t => logger.error(t, error.message))
      complete(error.message)
  }
}

class CustomerControllerImpl @Inject()
  (val actorSystem: ActorSystem,
   val executor: ExecutionContextExecutor) extends CustomerController {
  override implicit val materializer: Materializer = ActorMaterializer()(actorSystem)
  override val logger: LoggingAdapter = Logging(actorSystem, getClass)
  override lazy val customerService: CustomerService = implicitly[Global].injecor.getInstance(classOf[CustomerService])
}