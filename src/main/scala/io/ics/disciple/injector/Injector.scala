package io.ics.disciple.injector

import io.ics.disciple.dep._

import scala.reflect.ClassTag

trait Injector[T] {
  def apply(depIds: List[DepId[_]], cm: DepGraph): T
  protected def dep[P: ClassTag](id: DepId[P], cm: DepGraph): P = id match {
    case CTId(ct: ClassTag[P]) => cm[P]
    case NamedId(name, ct: ClassTag[P]) => cm[P](name)
  }
}
