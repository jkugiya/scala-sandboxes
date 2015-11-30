package jkugiya.nom.controllers

import akka.actor.ActorSystem
import akka.event.{LoggingAdapter, Logging}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, HttpRequest}
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.ExecutionContextExecutor

trait CustomerController {
  implicit val actorSystem: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: Materializer
  implicit val version: Int

  val logger:  LoggingAdapter

  def route: Route = {
    pathPrefix("customers") {
      pathEnd {
        get {
          parameter('word) { word =>
            // todo word api
            ???
          }
          // TODO wordが無かったらどうなる？
        }
        post {
          // register
          ???
        }
        put {
          // TODO update
          ???
        }
        delete {
          // TODO delete
          ???
        }
      } ~
      path(IntNumber) { id =>
        get {
          // get
          ???
        }
        delete {
          // delete
          ???
        }
      }
    }
  }
}
