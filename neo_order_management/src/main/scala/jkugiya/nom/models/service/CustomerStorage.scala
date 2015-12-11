package jkugiya.nom.models.service

import akka.actor.{OneForOneStrategy, Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import jkugiya.nom.models.dto.NotFoundError
import jkugiya.nom.models.dto.customer.{SearchCondition, UpdateCustomerDTO, RegisterCustomerDTO}
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.CustomerStorage._
import jkugiya.nom.models.service.infrastructure.UsesConnection
import jkugiya.nom.utils.neo4j.Nom

import scala.collection.immutable
import scala.concurrent.duration._

object CustomerStorage {
  trait ProcessResult
  trait SuccessResult extends ProcessResult
  trait FailResult extends ProcessResult
  // TODO もう少し整理する
  case class Register(dto: RegisterCustomerDTO)
  case class CustomerRenewed(customer: Customer)
  case class CustomerRemoved(id: Long)
  case class Update(dto: UpdateCustomerDTO)
  case object TelMe
  case class FindBy(condition: SearchCondition)
  case class FindOne(id: Long)
  case class Delete(id: Long)
  case class ThisIsAll(all: Seq[Customer])
  case class FindResult(result: Seq[Customer]) extends SuccessResult
  case class FindOneResult(result: Option[Customer]) extends SuccessResult
  case object Ack extends SuccessResult
  case object Nack extends FailResult
}

// TODO クラスタ化
class CustomerStorage extends Actor {

  val writerGateway = context.actorOf(Props[CustomerStorageWriterGateway], "writerGateway")
  val storageCache = context.actorOf(Props[CustomerStorageCache], "cache")
  // supervisorとしてだけ振る舞う
  override def receive: Actor.Receive = Actor.emptyBehavior
}

/**
  * 書き込みのゲートウェイ。
  */
class CustomerStorageWriterGateway extends Actor {
  import CustomerStorage._
  val storage = context.actorOf(Props[CustomerPersistentStorage], "persistentStorage")

  // TODO PersistentStorageはエラーが起きやすいのでここでCircuitBreakerを設ける。
  override def receive: Actor.Receive = {
    case registerMsg: Register =>
      storage ! registerMsg
      sender() ! Ack
    case updateMsg: Update =>
      storage ! updateMsg
      sender() ! Ack
  }
}


class CustomerStorageCache extends Actor {
  var cache: Seq[Customer] = Nil
  override def receive: Actor.Receive = {
    case ThisIsAll(all) =>
      cache = all
    case CustomerRenewed(customer) =>
      cache = cache.filter(_.id != customer.id) :+ customer
    case FindBy(condition) =>
      val word = condition.word
      val result = cache.filter { customer =>
        customer.name.indexOf(word) > 0 ||
        customer.email.indexOf(word) > 0 ||
        customer.tel.indexOf(word) > 0 ||
        customer.address.indexOf(word) > 0 ||
        customer.comment.indexOf(word) > 0
      }
      sender ! FindResult(result)
    case FindOne(id) =>
      val result = cache.find(_.id == id)
      sender ! FindOneResult(result)
  }
}
/**
  * Created by jkugi_000 on 2015/12/09.
  */
class CustomerPersistentStorage(val customerRepository: CustomerRepository) extends Actor with UsesConnection[Nom] {
  import CustomerStorage._

  val logger: LoggingAdapter = Logging(context.system, getClass)

  var receivers: immutable.Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case TelMe => withConnection { implicit con =>
      customerRepository.search("")// TODO まぁ、よくないのでfindAllとか
    } match {
      case Right(all) =>
        // イベントの通知先を追加する
        receivers = receivers + sender
        // TODO データが多い場合はもう少しやり方を考えないといけない
        sender ! ThisIsAll(all)
      case Left(cause) =>
        logger.error(s"Failed to find customers. cause=${cause}")
        sender ! Nack
    }
    case Register(dto) => withConnection { implicit con =>
      // 登録
      customerRepository.create(dto)
    } match {
      case Right(customer) =>
        sender ! Ack
        receivers.foreach(_ ! CustomerRenewed(customer))
      case Left(cause) =>
        sender ! Nack
        logger.error(s"Failed to update customer. customer=${dto}, cause=${cause}.")
    }
    case Update(dto) => withConnectionE { implicit con =>
      // 更新
      customerRepository.update(dto) match {
        case Some(customer) => Right(customer)
        case None => Left(NotFoundError("customer"))
      }
    } match {
      case Right(customer) =>
        sender ! Ack
        receivers.foreach(_ ! CustomerRenewed(customer))
      case Left(cause) =>
        sender ! Nack
        logger.error(s"Failed to update customer. customer=${dto}, cause=${cause}.")
    }
    case Delete(id: Long) => withConnection{ implicit con =>
      customerRepository.remove(id)
    } match {
      case Right(_) => sender ! Ack
      case Left(cause) =>
        sender ! Nack
        logger.error(s"Failed to update customer. customer_id=${id}, cause=${cause}.")
    }
  }
}
