package io.ics.disciple.util

import io.ics.disciple.dep._

import scala.reflect.runtime.universe

object Util {
  type TT[T] = universe.TypeTag[T]
  type Type = universe.Type

  def typeOf[T : TT]: Type = {
    val r = universe.typeOf[T]
    if (r =:= universe.typeOf[String]) universe.typeOf[String]
    else r
  }

  def getId[P: TT](name: Option[Symbol]) = name match {
    case Some(n) => NamedId(n, this.typeOf[P])
    case None    => TTId(this.typeOf[P])
  }
}
