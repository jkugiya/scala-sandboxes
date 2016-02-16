package jkugiya.bp

import akka.actor.{Actor, ActorRef, ActorLogging}
import akka.pattern.CircuitBreaker
import akka.pattern.ask
import akka.stream.actor.ActorPublisherMessage.{Cancel, Request}
import akka.stream.actor.ActorSubscriberMessage.{OnComplete, OnError, OnNext}
import akka.stream.actor.{OneByOneRequestStrategy, ActorPublisher, RequestStrategy, ActorSubscriber}
import akka.util.Timeout
import jkugiya.bp.DataRepository._
import scala.concurrent.duration._
import scala.util.{Success, Failure, Random}

object DataRepository {

  trait FindCondition

  case class FindById(id: Long) extends FindCondition

  case class Find(condition: FindCondition)

  case class Save(Data: Data)

  case object Ack

  trait FindResult

  case class SingleResult(Data: Data) extends FindResult

  case object NoResult extends FindResult

  case class MultiResult(Datas: Seq[Data])

  case object ReduceLoad
}

class DataRepository extends Actor {

  var all:Set[Data] = Set.empty

  var load = 1

  context.system.scheduler.schedule(0.second, 1.second, self, ReduceLoad)(context.dispatcher)

  def receive = {
    case ReduceLoad =>
      load = Math.max(10, load - 10)
    case FindById(id) =>
      // 読み取り処理。負荷に応じて時間がかかる
      Thread.sleep(load)
      val result =
        all.find(_.id == id).map(SingleResult) getOrElse NoResult
      sender ! result
      // 読み取りで負荷が上がる
      load += 1
    case Save(data) =>
      // 書き込み処理。書き込みの成否に関わらずAckをすぐに返してしまう。
      // つまり、クライアントは書き込みの遅延によるバックプレッシャーはかけられない。
      sender ! Ack
      all = all.filter(_.id != data.id) + data
      // 書き込みで負荷が上がる
      load += 2
  }
}

class DataRepositoryWrapper(datarepository: ActorRef) extends ActorSubscriber with ActorLogging {

  override protected def requestStrategy: RequestStrategy = OneByOneRequestStrategy

  implicit val timeout: Timeout = 5.seconds

  var waitTime = 0
  val circuiteBreaker: CircuitBreaker = new CircuitBreaker(
    context.system.scheduler,
    maxFailures = 5,
    callTimeout = 150.milliseconds,
    resetTimeout = 3.seconds
  )(context.dispatcher)

  override def receive: Actor.Receive = {
    case OnNext(msg @ Save(data)) =>
      import context.dispatcher
      val f = circuiteBreaker.withCircuitBreaker(datarepository ? msg)
      f.onComplete {
        case Success(_) =>
          log.debug("data save.data = {}", data)
        case Failure(t) =>
          log.error(t, "saving data was failed. data = {}", data)
      }
    case OnNext(msg @ FindById(id)) =>
      import context.dispatcher
      val f = circuiteBreaker.withCircuitBreaker(datarepository ? msg)
      f.onComplete {
        case Success(SingleResult(data)) =>
          log.debug("finished retrieving. data = {}", data)
        case Success(NoResult) =>
          log.debug("finished retrieving(none). id = {}", id)
        case Failure(t) =>
          log.error(t, "saving data was failed. id = {}", id)
      }
    case OnError(t) =>
      log.error(t, "caught error.")
    case OnComplete =>
      log.debug("completed.")
  }
}
class DataRepositoryWriter extends ActorPublisher[Save] with ActorLogging {

  override def receive: Actor.Receive = {
    // Subscriberからの「n個publishしてよい」というリクエスト
    case Request(n) =>
      log.debug("Requested count = {}.", n)
      while (isActive && totalDemand > 0) {
        val next = Data(Random.nextInt(1000), Random.nextString(10))
        log.debug("Next data = {}", next)
        onNext(Save(next))
      }
    case Cancel =>
      log.debug("Subscription was canceled.")
      context.stop(self)
    case m =>
      log.debug("Caught other message. message = {}", m)
  }
}

class DataRepositoryReader extends ActorPublisher[FindById] with ActorLogging {

  override def receive: Actor.Receive = {
    // Subscriberからの「n個publishしてよい」というリクエスト
    case Request(n) =>
      log.debug("Requested count = {}.", n)
      while (isActive && totalDemand > 0) {
        val next = Random.nextInt
        log.debug("Next data = {}", next)
        onNext(FindById(next))
      }
    case Cancel =>
      log.debug("Subscription was canceled.")
      context.stop(self)
    case m =>
      log.debug("Caught other message. message = {}", m)
  }
}
