package io.ics.disciple

import org.scalatest._

class InjectionTest extends WordSpec with Matchers {

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
        val depGraph = Module().
          bind(new Singleton(_)).singleton.
          bind(A("test")).
          bind(new Dependent[Singleton](_)).byName('dependent1).
          bind(new Dependent[Singleton](_)).byName('dependent2).
          build()

        val (d1, d2) = (depGraph[Dependent[Singleton]]('dependent1), depGraph[Dependent[Singleton]]('dependent2))

        println(depGraph)
        Singleton.singletonCounter shouldBe 1
        //check that d1 and d2 links to the same object
        d1.t.dt shouldBe d2.t.dt
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
        caught.getMessage shouldBe "Not found binding for {Type[io.ics.disciple.B]}"
      }

      "Throws an exception when no dep by name" in {
        val module = Module().bind {
          A("instanceA")
        }.bindNamed(None, Some('labelB)) {
          C(_: A, _: B, "instanceC")
        }

        val caught = intercept[IllegalStateException](module.build())
        caught.getMessage shouldBe "Not found binding for {Name[labelB], Type[io.ics.disciple.B]}"
      }

      "Throw an exception on building graph with cyclic dependency" in {

        val caught = intercept[IllegalStateException] {
          Module().
            bind(Dep1("test", _: Dep2)).
            bind(Dep2).
            bind(DepCycle).build()
        }

        println(caught.getMessage)
        caught.getMessage shouldBe "Dependency graph contains cyclic dependency: ( {Type[io.ics.disciple.DepCycle]} -> {Type[io.ics.disciple.Dep1]} -> {Type[io.ics.disciple.Dep2]} -> {Type[io.ics.disciple.DepCycle]} )"
      }

      "Throw an exception on building graph on empty module" in {

        val caught = intercept[IllegalStateException] {
          Module().build()
        }

        println(caught.getMessage)
        caught.getMessage shouldBe "Module is empty"
      }
    }

    "Polymorphic bindings" when {
      "Bind by common type" in {
        val binding =
          Module().
            bind(new ServiceImpl(): Service).
            build()

        binding[Service] shouldBe a[ServiceImpl]
      }
    }
  }
}
