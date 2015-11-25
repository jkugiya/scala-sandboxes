package jkugiya.implicit_example

import java.time.ZonedDateTime

import jkugiya.common.RevenueRecognition

/**
  * Created by jkugi_000 on 2015/11/25.
  */
trait Gateway {
  def findRecognizationsFor(contractId: Long, date: ZonedDateTime): Seq[RevenueRecognition]
}

object Gateway {
  implicit val gateway: Gateway = new GatewayImpl
}

class GatewayImpl extends  Gateway {
  def findRecognizationsFor(contractId: Long, date: ZonedDateTime): Seq[RevenueRecognition] = ???
}