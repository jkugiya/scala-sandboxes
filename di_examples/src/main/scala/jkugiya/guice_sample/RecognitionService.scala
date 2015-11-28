package jkugiya.guice_sample

import java.time.ZonedDateTime

import com.google.inject.Inject
import jkugiya.common.{Yen, Money}

/**
  * Created by jkugi_000 on 2015/11/28.
  */
trait RecognitionService {
  def recognizedRevenue(contractNumber: Long, asOf: ZonedDateTime): Money
}
class RecognitionServiceImpl @Inject() (private[guice_sample] val db: Gateway) extends RecognitionService {

  def recognizedRevenue(contractNumber: Long, asOf: ZonedDateTime): Money = {
    db.findRecognizationsFor(contractNumber, asOf).foldLeft(Yen(0): Money) {
      case (sum, revenuRecognition) => sum +(revenuRecognition.amount)
    }
  }
}
