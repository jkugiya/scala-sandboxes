package jkugiya.activepassive

import jkugiya.activepassive.KVStore.Database
import play.api.libs.json.{Json, JsValue}
import better.files._
import java.io.{File => JFile}

object KVStore {

  case class Database(seq: Int, kv: Map[String, JsValue])

  object Database {
    implicit val format = Json.format[Database]
  }

  def createFileBase(): KVStore = FileBaseKVStore


}

trait KVStore {
  def persist(name: String, seq: Int, kv: Map[String, JsValue]): Unit

  def readPersisted(name: String)
}

object FileBaseKVStore extends KVStore {

  private def current(name: String): File = s"./.database-$name.json".toFile

  override def persist(name: String, seq: Int, kv: Map[String, JsValue]): Unit = {
    val bytes = Json.stringify(Json.toJson(Database(seq, kv)))
    val next = s"./.database-$name.json.new".toFile
    next.write(bytes)
    next.moveTo(current(name))
  }

  override def readPersisted(name: String): Database = {
    val cur = current(name)
    if (cur.exists) Json.parse(cur.contentAsString).as[Database]
    else Database(0, Map.empty)
  }
}
