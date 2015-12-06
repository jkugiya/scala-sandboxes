package jkugiya.nom.models.service.infrastructure

import scala.util.control.Exception._
import jkugiya.nom.models.dto._
import jkugiya.nom.models.service._
import jkugiya.nom.utils.neo4j.{Connection, Namespace}

trait UsesConnection[N <: Namespace] {
  def withConnection[A](f: Connection[N] => A)(implicit connection: Connection[N]): Result[A] =
    allCatch either f(connection) match {
      case Left(t) => Left(new UnexpectedError(t))
      case Right(r) => Right(r)
    }
  def withConnectionE[A](f: Connection[N] => Result[A])(implicit connection: Connection[N]): Result[A] =
    allCatch either f(connection) match {
      case Left(t) => Left(new UnexpectedError(t))
      case Right(r) => r
    }

  def validateExists[A](result: Result[Option[A]], objectName: String): Result[A] = result match {
    case Left(t) => Left(t)
    case Right(None) => Left(NotFoundError(objectName))
    case Right(Some(r)) => Right(r)
  }
}
