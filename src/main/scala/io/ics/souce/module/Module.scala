package io.ics.souce.module

import io.ics.souce.dep._
import io.ics.souce.injector.SingletonInjector
import io.ics.souce.util._

import scala.annotation.tailrec

case class Module(protected val deps: HList) extends BindBoilerplate with BindNamedBoilerplate {

  def byName(name: Symbol) = {
    deps match {
      case (CTId(ct), dep) :: tail =>
        Module(NamedId(name, ct) -> dep :: tail)
      case _ =>
        throw new IllegalStateException("Last dependency already has name or module is empty")
    }
  }

  def singleton = {
    deps match {
      case (id: DepId[_], Dep(injector, depCts)) :: tail =>
        Module(id -> Dep(SingletonInjector(injector), depCts) :: tail)
      case _ =>
        throw new IllegalStateException("Last dependency can't be singleton")
    }
  }

  def combine(module: Module) = Module(deps ++ module.deps)

  def build() = DepGraph(tuples(deps).toMap)

  @tailrec
  private def tuples(hList: HList, acc: List[(DepId[_], Dep[_])] = Nil): List[(DepId[_], Dep[_])] =
    hList match {
      case (id: DepId[_], dep: Dep[_]) :: tail =>
        tuples(tail, id -> dep :: acc)
      case _ => acc
    }


}
object Module {
  def apply(): Module = Module(HNil)
}
