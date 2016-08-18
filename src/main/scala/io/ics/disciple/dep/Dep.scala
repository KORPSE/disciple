package io.k0rp53.disciple.dep

import io.k0rp53.disciple.injector.Injector
import io.k0rp53.disciple.util.HList

case class Dep[R](f: Injector[R], depCts: List[DepId[_]])
