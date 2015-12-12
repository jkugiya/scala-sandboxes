package jkugiya.nom.models.service

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import jkugiya.nom.models.dto.customer.{RegisterCustomerDTO, SearchCondition, UpdateCustomerDTO}
import jkugiya.nom.models.dto.{NotFoundError, UnexpectedError}
import jkugiya.nom.models.entity.Customer
import jkugiya.nom.models.service.CustomerStorage.ProcessResult
import jkugiya.nom.models.service.infrastructure.UsesConnection
import jkugiya.nom.utils.neo4j.Nom

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Await, Future}

// TODO 全体的にFutureをAPIにしたい
trait CustomerService extends UsesConnection[Nom] {
  val customerStorageGateway: ActorRef
  val customerStorageCache: ActorRef
  implicit val timeout: Timeout
  implicit val ec: ExecutionContext

  /**
    * 顧客を登録する
    * @param condition 登録情報
    * @return 登録結果
    */
  def register(condition: RegisterCustomerDTO): Result[Unit] = withConnectionE { implicit con =>
    val future = (customerStorageGateway ? CustomerStorage.Register(condition))
      .map(_ => Right(()))
      .recover({case t: Throwable => Left(new UnexpectedError(t))})
    Await.result(future, Duration.Inf)
  }


  /**
    * IDを指定して顧客を検索する
    * @param customerId -
    * @return 顧客
    */
  def findCustomer(customerId: Long): Result[Customer] =  {
    withConnectionE { implicit con =>
      val future = (customerStorageCache ? CustomerStorage.FindOne(customerId))
        .mapTo[ProcessResult]// TODO 多分できない
        .map(_ match {
          case CustomerStorage.FindOneResult(customer) =>
            customer.map(Right(_)).getOrElse(Left(NotFoundError("customer")))
        })
        .recover({case t: Throwable => Left(new UnexpectedError(t))})
      Await.result(future, Duration.Inf)
    }
  }

  /**
    * 顧客情報を更新する。
    * @param condition 顧客情報
    * @return 更新結果
    */
  def update(condition: UpdateCustomerDTO): Result[Unit] = withConnectionE { implicit con =>
    val future = (customerStorageCache ? CustomerStorage.Update(condition))
      .map(_ => Right(()))
      .recover({case t: Throwable => Left(new UnexpectedError(t))})
    Await.result(future, Duration.Inf)
  }

  /**
    * 顧客情報を検索する。
    * @param condition
    * @return 検索結果
    */
  def search(condition: SearchCondition): Result[Seq[Customer]] = withConnectionE { implicit con =>
    val future: Future[Result[Seq[Customer]]] = (customerStorageCache ? CustomerStorage.FindBy(condition))
      .mapTo[CustomerStorage.FindResult]
      .map(fr => Right(fr.result))
      .recover({case t: Throwable => Left(new UnexpectedError(t))})
    Await.result(future, Duration.Inf)
  }

  /**
    * 顧客情報を削除する
    * @param customerId -
    */
  def deleteCustomer(customerId: Long): Result[Unit] = withConnectionE { implicit con =>
    val future = (customerStorageCache ? CustomerStorage.Delete(customerId))
      .map(_ => Right(()))
      .recover({case t: Throwable => Left(new UnexpectedError(t))})
    Await.result(future, Duration.Inf)
  }
}

class CustomerServiceImpl (actorSystem: ActorSystem) extends CustomerService  {
  lazy val customerStorageGateway =
    Await.result(actorSystem.actorSelection("/user/customerStorage/writingGateway").resolveOne(), Duration.Inf)
  lazy val customerStorageCache =
   Await.result(actorSystem.actorSelection("/user/customerStorage/cache").resolveOne(), Duration.Inf)
  import scala.concurrent.duration._
  override implicit val timeout: Timeout = 3.seconds// TODO DI
  override implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global// TODO FIX
}