package io.k0rp53.disciple

import io.ics.disciple.injector.SingletonInjector

case class Module(protected val deps: List[(DepId[_], Dep[_])]) extends BindBoilerplate with BindNamedBoilerplate {

  def byName(name: Symbol) = {
    deps match {
      case (CTId(ct), dep: Dep[_]) :: tail =>
        Module((NamedId(name, ct), dep) :: tail)
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

  def build() = DepGraph(deps.toMap)


}
object Module {
  def apply(): Module = Module(Nil)
}
