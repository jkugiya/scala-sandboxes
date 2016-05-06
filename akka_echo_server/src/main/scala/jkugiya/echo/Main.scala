package jkugiya.echo

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.io.{IO, Tcp}
import akka.util.ByteString

/**
  * Created by jkugi_000 on 2016/04/08.
  */
object Main extends App {
  implicit val system = ActorSystem("echo")
  val serviceProps = Props[EchoService]
  val server = system.actorOf(Props(classOf[Server], serviceProps))
  system.awaitTermination()
  system.shutdown()
}

object Protocols {
  case object Finished
}
class Server(props: Props) extends Actor with ActorLogging {
  import akka.io.Tcp._
  import context.system
  val MAX_COUNT = 3
  var processingCount = 0

  IO(Tcp) ! Bind(self, new InetSocketAddress("localhost", 8080))

  def receive = {
    case b @ Bound(localAddress) =>
      log.info("Bouded {}.", localAddress)
    case CommandFailed(_: Bind) =>
      log.info("Command failed.")
      context.stop(self)
    case c @ Connected(remote, local) =>
      log.info("Received from {}.", remote)
      // リクエストごとに1つのサービスインスタンスを作る。
      // あるリクエストが他のリクエストの影響を受けなくなるので、サービスが
      // 考慮するべきことが減る。
      if (processingCount < MAX_COUNT) {
        sender() ! Register(context.actorOf(props))
        processingCount += 1
      } else {
        log.info("Rejected {}", remote)
        // Closeを送ることで接続を切断できる。
        sender() ! Close
      }
    case Protocols.Finished =>
      processingCount -= 1
  }
}

class EchoService extends Actor with ActorLogging {
  import akka.io.Tcp._
  var buffer: ByteString = ByteString.empty
  def receive: Actor.Receive = {
    case Received(data) =>
      buffer = buffer ++ data
      if (data.utf8String == System.lineSeparator()) {
        sender() ! Write(buffer)
        buffer = ByteString.empty
      }
    case PeerClosed =>
      log.info("closed.")
      context.parent ! Protocols.Finished
      context stop self
  }
}