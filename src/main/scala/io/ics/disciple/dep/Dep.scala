package io.ics.disciple.dep

import io.ics.disciple.injector.Injector

case class Dep[R](f: Injector[R], depIds: List[DepId])
