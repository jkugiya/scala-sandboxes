package jkugiya.factory_example


trait Factory {
  val gateway: Gateway
  val recognitionService: RecognitionService
}

/**
  * Created by jkugi_000 on 2015/11/28.
  */
class FactoryImpl(repos: Repositories) extends Factory {
  import repos._
  lazy val recognitionService = new RecognitionService()
  lazy val gateway: Gateway = new GatewayImpl()
}

object Factory {
  implicit val impl: Factory = new FactoryImpl(RepositoriesForProduction)
  def apply(repos: Repositories) = new FactoryImpl(repos)
}



