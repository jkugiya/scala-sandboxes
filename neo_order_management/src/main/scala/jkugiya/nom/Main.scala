package jkugiya.nom

import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.server.{Route, RequestContext}
import akka.stream.scaladsl.Sink
import akka.stream.{ActorMaterializer, Materializer}
import jkugiya.nom.controllers.CustomerController
import jkugiya.nom.models.service.CustomerService
import akka.http.scaladsl.server.Directives._
import jkugiya.nom.utils.Global
import jkugiya.nom.utils.neo4j.{Connection, Nom}

import scala.concurrent.ExecutionContextExecutor

/**
  * Created by jkugi_000 on 2015/12/06.
  */
object Main extends App {
  val global = implicitly[Global]
  implicit val actorSystem =  ActorSystem()
  val service = new ConcreteService
  service.start()
  sys.addShutdownHook {
    actorSystem.shutdown()
    val connection = implicitly[Global].injecor.getInstance(classOf[Connection[Nom]])
    connection.underlyingClient.close()
  }
  class ConcreteService(implicit val actorSystem: ActorSystem) extends CustomerController {
    override val customerService: CustomerService = global.injecor.getInstance(classOf[CustomerService])
    override implicit val executor: ExecutionContextExecutor = actorSystem.dispatcher// TODO 当然よくない
    override implicit val materializer: Materializer = ActorMaterializer()
    override val logger: LoggingAdapter = Logging(actorSystem, getClass)


    def start(): Unit = {
      Http().bindAndHandle(Route.handlerFlow(this.route), "localhost", 8080)
    }
  }
}
