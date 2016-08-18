package io.k0rp53.disciple.dep

import scala.reflect.ClassTag

sealed trait DepId[T]

case class CTId[T](ct: ClassTag[T]) extends DepId[T] {
  override def toString: String = s"{Class[$ct]}"
}

case class NamedId[T](name: Symbol, ct: ClassTag[T]) extends DepId[T] {
  override def toString: String = s"{Name[${name.name}], Class[$ct]}"
}
