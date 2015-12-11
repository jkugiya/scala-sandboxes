package jkugiya.nom.utils

import akka.actor.ActorSystem
import com.google.inject.{Scope, Injector, AbstractModule, Singleton}
import jkugiya.nom.controllers.{CustomerControllerImpl, CustomerController}
import jkugiya.nom.models.repository.{CustomerRepositoryImpl, CustomerRepository}
import jkugiya.nom.models.service.{CustomerServiceImpl, CustomerService}
import jkugiya.nom.utils.neo4j.{ConnectionImpl, Nom, Connection}
import org.anormcypher.Neo4jREST
import play.api.libs.ws._

trait NomModule extends AbstractModule {
}

object NomModule {
  implicit lazy val module: NomModule = new NomModule {
    override def configure(): Unit = {
      val global = implicitly[Global]
      val config = global.config
      implicit val ws = ning.NingWSClient()
      val connection = Neo4jREST(
        host = config.getString("nom.neo4j.host"),
        port = config.getInt("nom.neo4j.port"),
        path = if (config.hasPath("nom.neo4j.path")) config.getString("nom.neo4j.path") else "/db/data/",
        username = config.getString("nom.neo4j.username"),
        password = config.getString("nom.neo4j.password")
      )

      val actorSystem = ActorSystem("nom_order_management")
      bind(classOf[ActorSystem]).toInstance(actorSystem)

      val databaseExecutionContext = scala.concurrent.ExecutionContext.Implicits.global// TODO
      bind(classOf[Connection[Nom]]).toInstance(new ConnectionImpl[Nom](connection, databaseExecutionContext, ws))

      bind(classOf[CustomerRepository]).toInstance(CustomerRepositoryImpl)

      // たまたまCustomerRepositoryImplがobjectで宣言可能だからよいが、依存性の複雑なクラスだったらキツい。
      // Actorを使う場合、
      bind(classOf[CustomerService])
        .toInstance(new CustomerServiceImpl(
          actorSystem
        ))

      val controllerExecutionContext = scala.concurrent.ExecutionContext.Implicits.global// TODO
      bind(classOf[CustomerController]).toInstance(new CustomerControllerImpl(actorSystem, controllerExecutionContext))
    }
  }

}
