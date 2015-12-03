package jkugiya.nom.models

import jkugiya.nom.models.dto.customer.NomError
import jkugiya.nom.utils.neo4j._
import jkugiya.nom.utils.ResultHandler._

/**
  * Created by jkugi_000 on 2015/12/04.
  */
package object service {

  type Result[A] = Either[NomError, A]

}
