package jkugiya.models.view

/**
  * Created by jkugi_000 on 2015/11/27.
  */
trait ServiceResponse extends Serializable {
  val status: ServiceResponse.Status
  val exntendedStatus: Int
}

object ServiceResponse {
  type Status = ServiceStatus
}

private[view] case class ServiceStatus(n: Int) extends AnyVal
object Status {
  val Success = ServiceStatus(0)
  val Fail = ServiceStatus(1)
}
