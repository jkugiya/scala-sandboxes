package jkugiya.services

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by jkugi_000 on 2015/11/25.
  */
trait TimeService {

  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  val logger: LoggingAdapter

  def currentTime(formatter: DateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME): String = {
    val now = ZonedDateTime.now()
    now.format(formatter)
  }

  def route: Route =
    get {
      pathSingleSlash {
        complete {
          currentTime()
        }
      }
    }
}
