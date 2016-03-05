package jkugiya.activepassive

import akka.actor.Actor.Receive
import akka.actor._
import akka.cluster.Cluster
import play.api.libs.json.JsValue
import scala.collection.mutable
import scala.concurrent.duration._

import scala.collection.immutable.{TreeMap}
import scala.util.Random

object ActivePassive {

  import ReplicationProtocols._

  import KVStore._

  /** 複製する更新情報 */
  private case class Replicate(seq: Int, key: String, value: JsValue, replyTo: ActorRef)

  /** 複製した更新情報の順序 */
  private case class Replicated(seq: Int)

  /** Tick */
  private case object Tick

  /**
    * [[replyTo]]に指定したレプリカに今持っている状態を引き継ぐ
    * @param replyTo
    */
  private case class TakeOver(replyTo: ActorRef)

  /**
    * 受け取った状態で初期化する
    */
  private case class InitialState(map: Map[String, JsValue], seq: Int)

  /**
    * Active replica.
    * @param localReplica
    * @param replicationFactor
    * @param maxQueueSize
    */
  class Active(localReplica: ActorRef,
               replicationFactor: Int,
               maxQueueSize: Int) extends Actor with Stash with ActorLogging {

    /**
      *
      */
    private val MaxOutstanding = maxQueueSize / 2

    /**
      * このレプリカが持っているデータ
      */
    private var theStore: Map[String, JsValue] = Map.empty

    /** シーケンスのジェネレータ */
    private var seqNr: Iterator[Int] = Iterator.empty

    /** レプリケートする予定の更新情報 */
    private val toReplicate: mutable.Queue[Replicate] = mutable.Queue.empty[Replicate]

    /** レプリケートしている途中で、Passive Replicaからの完了通知を待っているキュー */
    private var replicating = TreeMap.empty[Int, (Replicate, Int)]

    private var rejected = 0

    private val cluster = Cluster(context.system)

    import context.dispatcher

    val timer = context.system.scheduler.schedule(1.second, 1.second, self, Tick)

    override def postStop = timer.cancel()// Tickし続けないようにする。

    override def receive: Receive = {
      case InitialState(m, s) =>
        log.info("シーケンス番号{}から引き継いだ。", s)
        theStore = m
        seqNr = Iterator from s
        context.become(running)
        unstashAll()
      case _ =>
        stash()
    }

    val running: Receive = {
      case p @ Put(key, value, replyTo) =>
        // 更新
        if (toReplicate.size < MaxOutstanding) {
          toReplicate.enqueue(Replicate(seqNr.next(), key, value, replyTo))
          replicate()
        } else {
          // 今は更新をReplicateできないときは、拒否応答を返す
          rejected += 1
          replyTo ! PutRejected(key, value)
        }
      case Get(key, replyTo) =>
        // 値の取得
        replyTo ! GetResult(key, theStore.get(key))
      case Tick =>
        // 内部のAPI
        // 定期的にdissminateしてPassiveレプリカに状態を反映する
        replicating.valuesIterator foreach {
          case (replicate, _) => disseminate(replicate)
        }
        if (rejected > 0) {
          log.info("{}個のPUTが拒否された。", rejected)
          rejected = 0
        }
      case Replicated(confirm) =>
        for {
          (rep, n) <- replicating.get(confirm)
        } {
          if (n == 1) {
            // replicationFactorの数だけレプリケーションが終わった
            // レプリケーションの進行中のキュー(= replicating)から取り除いて、
            // 自分のメモリにもPUTされたデータを反映する
            replicating -= confirm
            theStore += rep.key -> rep.value
            rep.replyTo ! PutConfirmed(rep.key, rep.value)
          } else {
            // 進行中のキューを更新
            // 1つレプリケーションが終わったので、残数をデクリメントする
            replicating += confirm -> (rep, n - 1)
          }
          replicate()
        }
    }

    private def replicate(): Unit =
      if (replicating.size < MaxOutstanding && toReplicate.nonEmpty) {
        // まだレプリケートできる状態(= レプリケートしている途中のものの数が閾値を超えていない)
        // 進行中のキュー(= replicating)に更新対称のデータ(= r)といくつ複製するか(= replicationFactor)を設定する
        val r = toReplicate.dequeue
        replicating += r.seq -> (r, replicationFactor)
        disseminate(r)
      }

    // リモートのクラスタに更新対称のデータを送り付ける
    private def disseminate(r: Replicate): Unit = {
      val req = r.copy(replyTo = self)
      cluster.state.members.foreach(m => replicaOn(m.address) ! req)
    }

    private def replicaOn(addr: Address): ActorSelection =
      context.actorSelection(localReplica.path.toStringWithAddress(addr))
  }

  private case class GetSingle(seq: Int, replyTo: ActorRef)
  private case class GetFull(replyTo: ActorRef)
  private case object DoConsolidate

  class Passive(askAroundCount: Int, askAroundInterval: FiniteDuration, maxLag: Int) extends Actor with ActorLogging {


    private val applied = mutable.Queue.empty[Replicate]

    private var awatingInitialState = Option.empty[ActorRef]

    val cluster = Cluster(context.system)
    val name = cluster.selfAddress.toString.replaceAll("[:/]", "_")
    val random = new Random

    private var tickTask = Option.empty[Cancellable]

    def scheduleTick() = {
      tickTask.foreach(_.cancel())
      tickTask = Some(context.system.scheduler.scheduleOnce(askAroundInterval, self, DoConsolidate)(context.dispatcher))
    }

    override def receive: Receive = readPersisted(name) match {

    }
  }
}
