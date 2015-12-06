package jkugiya.nom.models.service

import jkugiya.nom.models.dto.{NotFoundError, NomError}
import jkugiya.nom.models.dto.customer.{SearchCondition, UpdateCustomerDTO, RegisterCustomerDTO}
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.repository.CustomerRepository
import jkugiya.nom.models.service.infrastructure.UsesConnection
import jkugiya.nom.utils.neo4j.{Nom}


trait CustomerService extends UsesConnection[Nom] {
  val customerRepository: CustomerRepository

  /**
    * 顧客を登録する
    * @param condition 登録情報
    * @return 登録結果
    */
  def register(condition: RegisterCustomerDTO): Result[Unit] = withConnection { implicit con =>
    customerRepository.create(condition)
  }

  /**
    * IDを指定して顧客を検索する
    * @param customerId -
    * @return 顧客
    */
  def findCustomer(customerId: Long): Result[Customer] =  {
    val result = withConnection { implicit con =>
      customerRepository.searchBy(customerId)
    }
    validateExists(result, "customer")
  }

  /**
    * 顧客情報を更新する。
    * @param condition 顧客情報
    * @return 更新結果
    */
  def update(condition: UpdateCustomerDTO): Result[Unit] = withConnection { implicit con =>
    customerRepository.update(condition)
  }

  /**
    * 顧客情報を検索する。
    * @param condition
    * @return 検索結果
    */
  def search(condition: SearchCondition): Result[Seq[Customer]] = withConnection { implicit con =>
    customerRepository.search(condition.word)
  }

  /**
    * 顧客情報を削除する
    * @param customerId -
    */
  def deleteCustomer(customerId: Long): Result[Unit] = withConnection { implicit con =>
    customerRepository.remove(customerId)
  }
}
