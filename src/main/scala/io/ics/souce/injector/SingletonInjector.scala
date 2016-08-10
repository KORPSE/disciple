package io.ics.souce.injector

import io.ics.souce.dep.DepGraph
import io.ics.souce.util.HList

case class SingletonInjector[T](underlying: Injector[T]) extends Injector[T] {
  @volatile
  private var value: Option[T] = None
  def apply(argTags: HList, cm: DepGraph): T =
    value match {
      case None    =>
        value synchronized {
          value match {
            case None    =>
              val r = underlying(argTags, cm)
              value = Some(r)
              r
            case Some(v) =>
              v
          }
        }
      case Some(v) =>
        v
    }
}
