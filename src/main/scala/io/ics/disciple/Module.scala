package io.ics.disciple

import io.ics.disciple.dep._
import io.ics.disciple.injector.SingletonInjector
import io.ics.disciple.module._

case class Module(deps: List[(DepId, Dep[_])],
                  nonLazyIds: List[DepId] = Nil) extends BindBoilerplate with BindNamedBoilerplate {

  def byName(name: Symbol) = {
    deps match {
      case (TTId(tt), dep: Dep[_]) :: tail =>
        copy((NamedId(name, tt), dep) :: tail)
      case _                               =>
        throw new IllegalStateException("Last bound dependency already has name or module is empty")
    }
  }

  def singleton = {
    deps match {
      case (id: DepId, Dep(injector, depIds)) :: tail =>
        copy(id -> Dep(SingletonInjector(injector), depIds) :: tail)
      case _ =>
        throw new IllegalStateException("Last bound dependency can't be singleton")
    }
  }

  def nonLazy = {
    deps match {
      case (id: DepId, Dep(_: SingletonInjector[_], _)) :: tail =>
        Module(deps, id :: nonLazyIds)
      case _ =>
        throw new IllegalStateException("Only singleton dependency might be non-lazy")
    }
  }

  def combine(module: Module) = Module(deps ++ module.deps, nonLazyIds ++ module.nonLazyIds)

  def build() = {
    val binding = DepGraph(deps.toMap)
    nonLazyIds foreach { id =>
      binding.getResult(id)
    }
    binding
  }


}

object Module {
  def apply(): Module = Module(Nil)
}
