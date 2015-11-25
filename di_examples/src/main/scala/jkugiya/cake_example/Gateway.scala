package jkugiya.cake_example

import java.time.ZonedDateTime

import jkugiya.common.RevenueRecognition

/**
  * Created by jkugi_000 on 2015/11/25.
  */
class GatewayComponent {
  val gateway = new Gateway
  class Gateway {
    def findRecognizationsFor(contractId: Long, date: ZonedDateTime): Seq[RevenueRecognition] = ???
  }
}
