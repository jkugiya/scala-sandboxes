package jkugiya.cake_example

import java.time.ZonedDateTime

import jkugiya.common.{Yen, Money}

/**
  * Created by jkugi_000 on 2015/11/25.
  */
trait RecognitionServiceComponent { self: GatewayComponent =>
  val recognitionService = new RecognitionService
  class RecognitionService {
    def recognizedRevenue(contractNumber: Long, asOf: ZonedDateTime): Money = {
      self.gateway.findRecognizationsFor(contractNumber, asOf).foldLeft(Yen(0): Money) {
        case (sum, revenuRecognition) => sum +(revenuRecognition.amount)
      }
    }
  }
}
