package io.ics.disciple

case class A(label: String)
case class B(a: A, label: String)
case class C(a: A, b: B, label: String)

class Dependent[T](val t: T) {
  override def toString: String = s"[dependent from $t]"
}

case class Dep1(label: String, d: Dep2)
case class Dep2(d: DepCycle)
case class DepCycle(d: Dep1)

class Singleton(a: A) {
  val dt = System.nanoTime()
  Singleton.singletonCounter += 1
}
object Singleton {
  var singletonCounter = 0
}

trait Service
class ServiceImpl extends Service
