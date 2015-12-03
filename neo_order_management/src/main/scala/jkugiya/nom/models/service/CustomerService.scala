package jkugiya.nom.models.service

import jkugiya.nom.models.dto.customer.RegisterCustomerDTO
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.infrastructure.UsesConnection
import jkugiya.nom.utils.neo4j.{Nom}


trait CustomerService extends UsesConnection[Nom] {
  val customerRepository: CustomerRepository

  def register(dto: RegisterCustomerDTO): Result[Unit] = withConnection { implicit con =>
    customerRepository.create(dto)
  }
}
