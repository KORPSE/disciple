package io.ics.souce.injector

import io.ics.souce.dep._
import io.ics.souce.util._

import scala.reflect.ClassTag

trait Injector[T] {
  def apply(argTags: HList, cm: DepGraph): T
  protected def dep[P: ClassTag](id: DepId[P], cm: DepGraph): P = id match {
    case CTId(ct: ClassTag[P]) => cm[P]
    case NamedId(name, ct: ClassTag[P]) => cm[P](name)
  }
}
