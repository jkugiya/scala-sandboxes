package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.javadsl.model.RequestEntity
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{FormData, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.{ActorMaterializer, Materializer}
import jkugiya.nom.models.dto.customer.SearchCondition
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.service.CustomerService
import org.mockito.{Matchers => m, Mockito}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor}

/**
  * Created by jkugi_000 on 2015/12/06.
  */
class CustomerControllerSpec extends FeatureSpec
with Matchers
with GivenWhenThen
with ScalatestRouteTest
with MockitoSugar {

  class CustomerControllerStub(_system: ActorSystem) extends CustomerController {
    override val actorSystem: ActorSystem = system
    override val customerService: CustomerService = mock[CustomerService]
    override implicit val executor: ExecutionContextExecutor = _system.dispatcher
    override implicit val materializer: Materializer = ActorMaterializer()
    override val logger: LoggingAdapter = mock[LoggingAdapter]
  }

  feature("ルーティングのテスト") {
    scenario("/customers: Get()") {
      val controller = new CustomerControllerStub(system)
      Get("/customers/") ~> controller.route ~> check {
        status should be (StatusCodes.OK)
        // TODO add assert
      }
    }
    scenario("/customers: Get with word") {
      val controller = new CustomerControllerStub(system)
      Mockito
        .when(controller.customerService.search(m.anyObject()))
        .thenReturn(Right(Seq(mock[Customer])))
      Get("/customers?word=test") ~> controller.route ~> check {
        status should be (StatusCodes.OK)
        Mockito
          .verify(controller.customerService, Mockito.times(1))
          .search(SearchCondition("test"))
        // TODO add assert
      }
    }
    scenario("/customers: Post") {
      val controller = new CustomerControllerStub(system)
      val params = Map(
        "name" -> "testName",
        "email" -> "testEmail",
        "tel" -> "00000000000",
        "address" -> "testAddress",
        "comment" -> "testComment"
      )
      val entity = Await.result(Marshal(FormData(params)).to[RequestEntity], Duration.Inf)
      val req = Post("/customers").withEntity(entity)
      req ~> controller.route ~> check {
        status should be (StatusCodes.OK)
        Mockito
          .verify(controller.customerService, Mockito.times(1))
          .register(m.anyObject())
      }
    }
    scenario("/customers/create: Get") {
      val controller =  new CustomerControllerStub(system)
      val req = Get("/customers/create")
      req ~> controller.route ~> check {
        status should be (StatusCodes.OK)
      }
    }
  }


}

