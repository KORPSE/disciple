package io.ics.disciple.dep

import io.ics.disciple.util.Util._

sealed trait DepId

case class TTId(tpe: Type) extends DepId {
  override def toString: String = s"{Type[$tpe]}"
}

case class NamedId(name: Symbol, tpe: Type) extends DepId {
  override def toString: String = s"{Name[${name.name}], Type[$tpe]}"
}
