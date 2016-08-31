package io.ics.disciple.injector

import io.ics.disciple.dep._

case class SingletonInjector[T](underlying: Injector[T]) extends Injector[T] {
  @volatile
  private var value: Option[T] = None
  def apply(depIds: List[DepId], cm: DepGraph): T =
    value match {
      case None =>
        value synchronized {
          value match {
            case None =>
              val instance = underlying(depIds, cm)
              value = Some(instance)
              instance
            case Some(existing) =>
              existing
          }
        }
      case Some(existing) =>
        existing
    }
}
