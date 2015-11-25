package jkugiya.example1

import java.time.ZonedDateTime

import jkugiya.common.{Yen, Money}

/**
  * Created by jkugi_000 on 2015/11/25.
  */
class RecognitionService {

  private[example1] val db: Gateway = new GatewayImpl

  def recognizedRevenue(contractNumber: Long, asOf: ZonedDateTime): Money = {
    db.findRecognizationsFor(contractNumber, asOf).foldLeft(Yen(0): Money) {
      case (sum, revenuRecognition) => sum +(revenuRecognition.amount)
    }
  }
}
