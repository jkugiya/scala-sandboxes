package jkugiya.nom.models

import jkugiya.nom.models.dto.customer.UpdateCustomerDTO
import jkugiya.nom.models.entity.Customer

trait Mapper[-From, +To] {
  def map(from: From): To
}

object Mapper {
  implicit val uc2c: Mapper[Customer, UpdateCustomerDTO] =
    new Mapper[Customer, UpdateCustomerDTO] {
      def map(from: Customer): UpdateCustomerDTO = UpdateCustomerDTO(
        id = from.id,
        name = from.name,
        email = from.email,
        tel = from.tel,
        address = from.address,
        comment = from.comment
      )
    }
}

object ConcreteMapper extends Mappers

trait Mappers {
  def map[From, To](from: From)(implicit mapper: Mapper[From, To]): To
    = mapper.map(from)
}