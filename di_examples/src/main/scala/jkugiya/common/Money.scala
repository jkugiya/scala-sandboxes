package jkugiya.common

/**
  * Created by jkugi_000 on 2015/11/25.
  */
trait Money {
  val amount: Int
  def +(another: Money): Money
}

case class Yen(amount: Int) extends Money {
  def +(another: Money): Money =  another match {
    case Yen(anotherAmount) => Yen(amount + anotherAmount)
    case _ => ???
  }
}
