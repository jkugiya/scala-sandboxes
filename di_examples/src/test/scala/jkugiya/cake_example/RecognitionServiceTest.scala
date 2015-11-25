package jkugiya.cake_example

import java.time.ZonedDateTime

import jkugiya.common.{Yen, RevenueRecognition}
import jkugiya.example1.Gateway
import org.mockito.{ Mockito, Matchers => m }
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, GivenWhenThen, FeatureSpec}

/**
  * Created by jkugi_000 on 2015/11/25.
  */
class RecognitionServiceTest extends FeatureSpec
  with GivenWhenThen
  with Matchers
  with MockitoSugar {

  feature("Getting recognized revenue.") {
    def createServiceComponent() = new {} with GatewayComponent with RecognitionServiceComponent {
      override val gateway: Gateway = mock[Gateway]
    }

    scenario("データを取得できた場合") {
      Given("適当な引数")
      val contractNumber = 3L
      val asOf = ZonedDateTime.now()
      Given("データベースからデータを取得")
      val sc = createServiceComponent()
      val service = sc.recognitionService
      val db = sc.gateway
      val amount1 = 100
      val amount2 = 200
      val amount3 = 300
      val recognitions = Seq(
        RevenueRecognition(1, Yen(amount1), ZonedDateTime.now),
        RevenueRecognition(2, Yen(amount2), ZonedDateTime.now),
        RevenueRecognition(3, Yen(amount3), ZonedDateTime.now)
      )
      Mockito
        .when(db.findRecognizationsFor(m.anyObject(), m.anyObject()))
        .thenReturn(recognitions)

      When("処理を呼び出す")
      val result = service.recognizedRevenue(contractNumber, asOf)

      Then("データべスから取得した値の合計値が返される")
      result.amount should be (amount1 + amount2 + amount3)
    }
  }
}
