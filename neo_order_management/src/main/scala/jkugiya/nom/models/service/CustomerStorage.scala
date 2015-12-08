package jkugiya.nom.models.service

import akka.actor.{ActorRef, Props, Actor}
import jkugiya.nom.models.dto.customer.{UpdateCustomerDTO, RegisterCustomerDTO}
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.infrastructure.UsesConnection

object CustomerStorage {
  case class Register(dto: RegisterCustomerDTO)
  case class CustomerRegistered(customer: Customer)
  case class CustomerUpdated(customer: Customer)
  case class Update(dto: UpdateCustomerDTO)
  case class TelMe(me: ActorRef)
  case object Ack
  case object Nack
}
/**
  * Created by jkugi_000 on 2015/12/09.
  */
class CustomerStorage(val customerRepository: CustomerRepository) extends Actor with UsesConnection {
  import CustomerStorage._

  var receivers: Set[ActorRef] = Set.empty

  override def receive: Receive = {
    case Register(dto) => withConnection { implicit con =>
      customerRepository.create(dto)
    } match {
      case Right(_) =>
        sender ! Ack
        receivers.foreach(_ ! CustomerRegistered)
      case Left(_) => sender ! Nack
    }
    case Update(dto) => withConnection { implicit con =>
      customerRepository.update(dto)
    } match {
      case Right(_) => sender ! Ack
      case Left(_) => sender ! Nack
    }
  }
}
