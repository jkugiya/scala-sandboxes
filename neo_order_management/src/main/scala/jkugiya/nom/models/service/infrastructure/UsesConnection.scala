package jkugiya.nom.models.service.infrastructure

import jkugiya.nom.models.service._
import jkugiya.nom.utils.ResultHandler._
import jkugiya.nom.utils.neo4j.{Connection, Namespace}

/**
  * Created by jkugi_000 on 2015/12/04.
  */
trait UsesConnection[N <: Namespace] {
  def withConnection[A](f: Connection[N] => A)(implicit connection: Connection[N]): Result[A] =
    handleResult(f(connection))
}
