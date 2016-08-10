package io.ics.souce.dep

import io.ics.souce.injector.Injector
import io.ics.souce.util.HList

case class Dep[R](f: Injector[R], depCts: HList)
