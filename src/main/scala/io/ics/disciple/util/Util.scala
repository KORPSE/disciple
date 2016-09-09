package io.ics.disciple.util

import io.ics.disciple.dep._
import io.ics.disciple.labels._

import scala.reflect.runtime.universe

object Util {
  type TT[T] = universe.TypeTag[T]
  type Type = universe.Type
  type L = PossibleLabel

  def typeOf[T : TT]: Type = {
    val r = universe.typeOf[T]
    if (r =:= universe.typeOf[String]) universe.typeOf[String]
    else r
  }

  def getId[P: TT](name: PossibleLabel) = name match {
    case Label(n) => NamedId(n, this.typeOf[P])
    case NoLabel  => TTId(this.typeOf[P])
  }
}
