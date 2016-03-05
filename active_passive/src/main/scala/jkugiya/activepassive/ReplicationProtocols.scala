package jkugiya.activepassive

import akka.actor.ActorRef
import play.api.libs.json.JsValue

/**
  * traced from [[https://github.com/ReactiveDesignPatterns]]
  */
object ReplicationProtocols {
  trait Command
  trait Result

  case class Put(key: String, value: JsValue, replyTo: ActorRef)
  case class Get(key: String, replyTo: ActorRef)

  case class PutConfirmed(key: String, value: JsValue) extends Result
  case class PutRejected(key: String, value: JsValue) extends Result
  case class GetResult(key: String, value: Option[JsValue]) extends Result

}
