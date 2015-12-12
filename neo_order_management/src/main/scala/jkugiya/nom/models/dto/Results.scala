package jkugiya.nom.models.dto

trait NomError {
  val message: String
  val cause: Option[Throwable]
}

case class NotFoundError(objectName: String) extends NomError {
  val message = s"${objectName} is not found."
  val cause = None
}

abstract case class NomFault(message: String, t: Throwable) extends NomError {
  val cause = Some(t)
}

class UnexpectedError(t: Throwable) extends NomFault("Unexpected error occurred.", t)

