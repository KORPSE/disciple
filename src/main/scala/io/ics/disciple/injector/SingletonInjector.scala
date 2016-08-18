package io.ics.disciple.injector

import io.ics.disciple.dep._

case class SingletonInjector[T](underlying: Injector[T]) extends Injector[T] {
  @volatile
  private var value: Option[T] = None
  def apply(depIds: List[DepId[_]], cm: DepGraph): T =
    value match {
      case None    =>
        value synchronized {
          value match {
            case None    =>
              val r = underlying(depIds, cm)
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
