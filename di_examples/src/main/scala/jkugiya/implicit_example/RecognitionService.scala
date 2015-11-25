package jkugiya.implicit_example

import java.time.ZonedDateTime

import jkugiya.common.{Money, Yen}

/**
  * Created by jkugi_000 on 2015/11/25.
  */
class RecognitionService(implicit db: Gateway) {

  def recognizedRevenue(contractNumber: Long, asOf: ZonedDateTime): Money = {
    db.findRecognizationsFor(contractNumber, asOf).foldLeft(Yen(0): Money) {
      case (sum, revenuRecognition) => sum +(revenuRecognition.amount)
    }
  }
}

object RecognitionService extends RecognitionService