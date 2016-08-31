package io.ics.disciple.dep

import io.ics.disciple.injector.Injector

import io.ics.disciple.util.Util._

import scala.collection.immutable.ListSet

case class DepGraph private (map: Map[DepId, Dep[_]]) {
  override def toString = map.toString()

  def apply[R: TT]: R = getResult(TTId(typeOf[R])).asInstanceOf[R]
  def apply[R: TT](name: Symbol): R = getResult(NamedId(name, typeOf[R])).asInstanceOf[R]

  private def findCycleDfs(visited: ListSet[DepId], depIds: List[DepId]): Option[List[DepId]] =
    depIds match {
      case (id: DepId) :: tail =>
        if (visited contains id) Some(id :: visited.toList)
        else {
          map.get(id) match {
            case Some(Dep(_, deps)) =>
              findCycleDfs(visited + id, deps) orElse findCycleDfs(visited, tail)
            case _ =>
              throw new IllegalStateException(s"Not found binding for $id")
          }
        }
      case _ => None
    }

  private[disciple] def getResult(id: DepId): Any = {
    map.get(id) match {
      case Some(Dep(injector: Injector[_], depIds)) =>
        injector(depIds, this)
      case _ =>
        throw new IllegalStateException(s"Not found binding for $id")
    }
  }

  //test dep graph for correctness
  if (map.nonEmpty) {
    findCycleDfs(ListSet.empty, map.keys.toList) foreach {
      depList => throw new IllegalStateException(s"Dependency graph contains cyclic dependency: ( ${depList.reverse.mkString(" -> ")} )")
    }
  } else {
    throw new IllegalStateException(s"Module is empty")
  }
}
