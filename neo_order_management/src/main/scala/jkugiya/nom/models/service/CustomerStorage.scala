package jkugiya.nom.models.service

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingAdapter}
import jkugiya.nom.models.dto.NotFoundError
import jkugiya.nom.models.dto.customer.{UpdateCustomerDTO, RegisterCustomerDTO}
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.infrastructure.UsesConnection
import jkugiya.nom.utils.neo4j.Nom

import scala.collection.immutable

object CustomerStorage {
  case class Register(dto: RegisterCustomerDTO)
  case class CustomerRegistered(customer: Customer)
  case class CustomerUpdated(customer: Customer)
  case class Update(dto: UpdateCustomerDTO)
  case object TelMe
  case class ThisIsAll(all: Seq[Customer])
  case object Ack
  case object Nack
}

class CustomerStorageWriter extends Actor {
  override def receive: Actor.Receive = ???
}

class CustomerStorageCache extends Actor {
  override def receive: Actor.Receive = ???
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
        receivers.foreach(_ ! CustomerRegistered(customer))
      case Left(_) =>
        sender ! Nack
        logger.error(s"Failed to create customer. customer=${dto}")
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
        receivers.foreach(_ ! CustomerUpdated(customer))
      case Left(cause) =>
        sender ! Nack
        logger.error(s"Failed to update customer. customer=${dto}, cause=${cause}.")
    }
  }
}
