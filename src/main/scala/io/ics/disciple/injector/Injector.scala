package io.ics.disciple.injector

import io.ics.disciple.dep._

import io.ics.disciple.util.Util._

trait Injector[T] {
  def apply(depIds: List[DepId], cm: DepGraph): T
  protected def dep[P: TT](id: DepId, cm: DepGraph): P = id match {
    case TTId(_)          => cm[P]
    case NamedId(name, _) => cm[P](name)
  }
}
