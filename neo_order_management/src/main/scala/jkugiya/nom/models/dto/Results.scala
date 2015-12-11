package jkugiya.nom.models.dto

trait NomError {
  val message: String
}

case class NotFoundError(objectName: String) extends NomError {
  val message = s"${objectName} is not found."
}

abstract case class NomFault(message: String, cause: Throwable) extends NomError

class UnexpectedError(t: Throwable) extends NomFault("Unexpected error occurred.", t)

