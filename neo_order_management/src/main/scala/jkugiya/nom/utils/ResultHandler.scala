package jkugiya.nom.utils

import jkugiya.nom.models.dto.customer.{NomError, UnexpectedError}

import scala.util.{Failure, Success, Try}

object ResultHandler {

  def handleResult[A](f: => A): Either[NomError, A] = Try {
    f
  } match {
    case Success(result) => Right(result)
    case Failure(t) => Left(new UnexpectedError(t))
  }


  def handleEither[A](f: => Either[NomError, A]): Either[NomError, A] = Try {
    f
  } match {
    case Success(result) => result
    case Failure(t) => Left(new UnexpectedError(t))
  }
}
