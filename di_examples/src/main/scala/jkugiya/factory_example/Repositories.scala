package jkugiya.factory_example

/**
  * Created by jkugi_000 on 2015/11/28.
  */
trait Repositories {
  implicit val db: Gateway
  implicit val recognitionService: RecognitionService
}

object Repositories {
  implicit val impl: Repositories = RepositoriesForProduction
}

object RepositoriesForProduction extends Repositories {
  override implicit val db: Gateway = new GatewayImpl
  override implicit val recognitionService: RecognitionService = new RecognitionService()
}
