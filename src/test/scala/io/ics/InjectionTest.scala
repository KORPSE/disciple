package io.ics

import io.ics.disciple.Module
import org.scalatest._

object TestClasses {

  case class A(label: String)
  case class B(a: A, label: String)
  case class C(a: A, b: B, label: String)

  class Dependent[T](val t: T) {
    override def toString: String = s"[dependent from $t]"
  }

  case class Dep1(label: String, d: Dep2)
  case class Dep2(d: DepCycle)
  case class DepCycle(d: Dep1)
}

class InjectionTest extends WordSpec with Matchers {

  import TestClasses._

  "Module" when {

    "input is correct" should {
      "build dependency graph in the simplest case" in {
        val depGraph = Module().
          bind(A("instanceA")).
          bind(C(_: A, _: B, "instanceC")).
          bind(B(_: A, "instanceB")).
          build()
        val result = depGraph[C]

        result shouldBe C(A("instanceA"), B(A("instanceA"), "instanceB"), "instanceC")
      }


      "wire by name" in {
        val depGraph = Module().
          bindNamed(Some('labelA)) {
            A
          }.
          bindNamed(None, Some('labelB)) {
            B
          }.
          bindNamed(None, None, Some('labelC)) {
            C
          }.
          bind("instanceA").byName('labelA).
          bind("instanceB").byName('labelB).
          bind("instanceC").byName('labelC).
          build()

        val result = depGraph[C]
        result shouldBe C(A("instanceA"), B(A("instanceA"), "instanceB"), "instanceC")
      }

      "singleton creates exactly ones" in {

        var singletonCounter = 0
        class Singleton(a: A) {
          val dt = System.nanoTime()
          singletonCounter += 1
        }

        val depGraph = Module().
          bind(new Singleton(_: A)).singleton.
          bind(A("test")).
          bind(new Dependent(_: Singleton)).byName('dependent1).
          bind(new Dependent(_: Singleton)).byName('dependent2).
          build()

        val (d1, d2) = (depGraph[Dependent[Singleton]]('dependent1), depGraph[Dependent[Singleton]]('dependent2))

        println(depGraph)
        singletonCounter shouldBe 1
        //check that d1 and d2 links to the same object
        d1.t.dt shouldBe d2.t.dt
      }
    }
  }

  "Input is insufficient" should {
    "Throws an exception when no dep by class" in {
      val module = Module() bind {
        A("instanceA")
      } bind {
        C(_: A, _: B, "instanceC")
      }

      val caught = intercept[IllegalStateException](module.build())
      caught.getMessage shouldBe "Not found binding for {Class[io.ics.TestClasses$B]}"
    }

    "Throws an exception when no dep by name" in {
      val module = Module().bind {
        A("instanceA")
      }.bindNamed(None, Some('labelB)) {
        C(_: A, _: B, "instanceC")
      }

      val caught = intercept[IllegalStateException](module.build())
      caught.getMessage shouldBe "Not found binding for {Name[labelB], Class[io.ics.TestClasses$B]}"
    }

    "Throw an exception on building graph with cyclic dependency" in {

      val caught = intercept[IllegalStateException] {
        Module().
          bind(Dep1("test", _: Dep2)).
          bind(Dep2).
          bind(DepCycle).build()
      }

      println(caught.getMessage)
      caught.getMessage shouldBe "Dependency graph contains cyclic dependency: ( {Class[io.ics.TestClasses$DepCycle]} -> {Class[io.ics.TestClasses$Dep1]} -> {Class[io.ics.TestClasses$Dep2]} -> {Class[io.ics.TestClasses$DepCycle]} )"
    }

    "Throw an exception on building graph on empty module" in {

      val caught = intercept[IllegalStateException] {
        Module().build()
      }

      println(caught.getMessage)
      caught.getMessage shouldBe "Module is empty"
    }
  }
}
