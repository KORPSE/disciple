package io.ics.souce.dep

import io.ics.souce.injector.Injector
import io.ics.souce.util.Util._
import io.ics.souce.util.{HList, HNil, _}

import scala.collection.immutable.ListSet
import scala.reflect.ClassTag

case class DepGraph (map: Map[DepId[_], Dep[_]]) {
  override def toString = map.toString()

  def apply[R: ClassTag]: R = getResult(CTId(classTag[R]))
  def apply[R: ClassTag](name: Symbol): R = getResult(NamedId(name, classTag[R]))

  private def keysToHlist(seq: Iterable[DepId[_]]): HList = {
    seq.foldLeft(HNil: HList) {
      case (hList, id) => id :: hList
    }
  }

  private def findCycleDfs(visited: ListSet[DepId[_]], cts: HList): Option[List[DepId[_]]] =
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
      case Some(Dep(injector: Injector[_], depCts)) =>
        injector(depCts, this).asInstanceOf[R]
      case _ =>
        throw new IllegalStateException(s"Not found binding for $id")
    }
  }

  //test dep graph for correctness
  if (map.nonEmpty) {
    findCycleDfs(ListSet.empty, keysToHlist(map.keys)) foreach {
      depList => throw new IllegalStateException(s"Dependency graph contains cyclic dependency: ( ${depList.mkString(" <- ")} )")
    }
  } else {
    throw new IllegalStateException(s"Module is empty")
  }
}
