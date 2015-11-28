package jkugiya.guice_sample

import java.time.ZonedDateTime

import jkugiya.common.RevenueRecognition

/**
  * Created by jkugi_000 on 2015/11/28.
  */
trait Gateway {
  def findRecognizationsFor(contractId: Long, date: ZonedDateTime): Seq[RevenueRecognition]
}

class GatewayImpl extends  Gateway {
  def findRecognizationsFor(contractId: Long, date: ZonedDateTime): Seq[RevenueRecognition] = ???
}