package jkugiya.nom.models.dto.customer

trait NomError {
  val message: String
}

abstract case class NomFault(message: String, cause: Throwable) extends NomError

class UnexpectedError(t: Throwable) extends NomFault("Unexpected error occured.", t)
