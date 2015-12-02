package jkugiya.nom.models.dto.customer

case class RegisterCustomerDTO(name: String, email: String, tel: String, address: String, comment: String)

case class UpdateCustomerDTO(id: Long, name: String, email: String, tel: String, address: String, comment: String)

case class SearchCondition(word: String)
