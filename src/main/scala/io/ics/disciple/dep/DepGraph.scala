package io.ics.disciple.dep

import io.ics.disciple.injector.Injector
import io.ics.disciple.util.Util._

import scala.collection.immutable.ListSet
import scala.reflect.ClassTag

case class DepGraph private (map: Map[DepId[_], Dep[_]]) {
  override def toString = map.toString()

  def apply[R: ClassTag]: R = getResult(CTId(classTag[R]))
  def apply[R: ClassTag](name: Symbol): R = getResult(NamedId(name, classTag[R]))

  private def findCycleDfs(visited: ListSet[DepId[_]], cts: List[DepId[_]]): Option[List[DepId[_]]] =
    cts match {
      case (id: DepId[_]) :: tail =>
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

  private def getResult[R](id: DepId[R]): R = {
    map.get(id) match {
      case Some(Dep(injector: Injector[_], depIds)) =>
        injector(depIds, this).asInstanceOf[R]
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
