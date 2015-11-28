package jkugiya.guice_sample

import java.time.ZonedDateTime
import javax.inject.{Singleton, Provider}

import com.google.inject.{Guice, AbstractModule}
import jkugiya.common.{RevenueRecognition, Yen}
import org.mockito.{Matchers => m, Mockito}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

/**
  * Created by jkugi_000 on 2015/11/25.
  */
class RecognitionServiceTest extends FeatureSpec
  with GivenWhenThen
  with Matchers
  with MockitoSugar {
  class TestModule extends AbstractModule {
    override def configure(): Unit = {
      bind(classOf[Gateway]).toProvider(new Provider[Gateway] { def get() = mock[Gateway]}).in(classOf[Singleton])
      bind(classOf[RecognitionService]).to(classOf[RecognitionServiceImpl])
    }
  }
  def injector = Guice.createInjector(new TestModule())

  feature("Getting recognized revenue.") {
    scenario("データを取得できた場合") {
      Given("適当な引数")
      val contractNumber = 3L
      val asOf = ZonedDateTime.now()
      Given("データベースからデータを取得")
      val i = injector
      val service = i.getInstance(classOf[RecognitionService])
      val db = i.getInstance(classOf[Gateway])
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

      Mockito
        .verify(db, Mockito.times(1))
        .findRecognizationsFor(m.eq(contractNumber), m.eq(asOf))
    }
  }
}
